package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.audiomgmt.ActiveAudioRegistry;
import at.htlleonding.fabia.callmgmt.audiomgmt.PlaybackItem;
import at.htlleonding.fabia.callmgmt.audiomgmt.RecordingItem;
import at.htlleonding.fabia.callmgmt.util.AriUtil;
import at.htlleonding.fabia.callmgmt.util.CallState;
import at.htlleonding.fabia.callmgmt.util.FieldHandlingState;
import at.htlleonding.fabia.callmgmt.util.HandledState;
import at.htlleonding.fabia.client.coquibe.CoquiRequest;
import at.htlleonding.fabia.client.coquibe.SpeechGenerationService;
import at.htlleonding.fabia.client.formbe.FieldResponseService;
import at.htlleonding.fabia.client.formbe.dtos.Field;
import at.htlleonding.fabia.client.formbe.dtos.requests.FieldResponseCreationRequest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
@HandledState(CallState.FIELD)
public final class FieldHandler extends StateHandler {
    @RestClient
    SpeechGenerationService speechGenerationService;
    @RestClient
    FieldResponseService fieldResponseService;
    @Inject
    AriUtil ariUtil;
    @Inject
    ActiveAudioRegistry activeAudioRegistry;

    @Override
    protected Uni<Void> handleSingleItemAsync(CallSession session) {
        if (!session.fieldGroupValid()) {
            throw new IllegalStateException("No form or field selected");
        }

        session.addFields(session.getCurrentFieldGroup().getFields());

        if (session.fieldsEmpty()) {
            session.goToFieldGroup();
            return Uni.createFrom().voidItem();
        }

        if (!session.getFieldHandlingState().isRetry()) {
            session.tryIncreaseFieldIdx();
        }

        final String fieldSpeech = "field-speech";
        final String fieldDescription = "field-description";

        Field field = session.getCurrentField();
        CoquiRequest request = new CoquiRequest(field.getName(), fieldSpeech);

        Uni<Void> mainSpeech = speechGenerationService.generateSpeech(request);
        Uni<Void> descriptionSpeech = Uni.createFrom().voidItem();

        if (field.getDescription() != null && !field.getDescription().isBlank()) {
            descriptionSpeech = speechGenerationService.generateSpeech(new CoquiRequest(field.getDescription(), fieldDescription));
        }

        Uni<Void> finalDescriptionSpeech = descriptionSpeech;
        return ariUtil.startMohAsync(session.getBridgeId())
                .flatMap(v -> Uni.combine().all().unis(mainSpeech, finalDescriptionSpeech).asTuple())
                .invoke(tuple -> {
                    session.enqueueAudio(new PlaybackItem("field-intro", session.getBridgeId(), ariUtil, activeAudioRegistry));
                    session.enqueueAudio(new PlaybackItem(fieldSpeech, session.getBridgeId(), ariUtil, activeAudioRegistry));

                    if (field.getDescription() != null && !field.getDescription().isBlank()) {
                        session.enqueueAudio(new PlaybackItem(fieldDescription, session.getBridgeId(), ariUtil, activeAudioRegistry));
                    }
                }).replaceWithVoid().eventually(() -> ariUtil.endMohAsync(session.getBridgeId()));
    }

    @Override
    protected Uni<Void> handleRequestInputAsync(CallSession session) {
        final String typeDescriptionSpeech = "type-description";
        Field field = session.getCurrentField();

        Uni<Void> fieldDescriptionSpeech = Uni.createFrom().voidItem();

        if (field.getType().getDescription() != null && !field.getType().getDescription().isBlank()) {
            fieldDescriptionSpeech = ariUtil.startMohAsync(session.getBridgeId())
                    .chain(() -> speechGenerationService.generateSpeech(new CoquiRequest(field.getType().getDescription(), typeDescriptionSpeech)))
                    .invoke(() -> {
                        session.enqueueAudio(new PlaybackItem("please-consider", session.getBridgeId(), ariUtil, activeAudioRegistry));
                        session.enqueueAudio(new PlaybackItem(typeDescriptionSpeech, session.getBridgeId(), ariUtil, activeAudioRegistry));
                    }).eventually(() -> ariUtil.endMohAsync(session.getBridgeId()));
        }

        return fieldDescriptionSpeech.invoke(() -> {
            RecordingItem recording = new RecordingItem(session.getBridgeId(), ariUtil, activeAudioRegistry);
            session.getFieldHandlingState().setRecording(recording);
            session.enqueueAudio(recording);
        });
    }

    @Override
    protected Uni<Void> handleProcessInputAsync(CallSession session) {
        FieldHandlingState state = session.getFieldHandlingState();
        if (state.getRecording() == null) {
            throw new IllegalStateException("Tried to handle an input without a recording");
        }

        Field current = session.getCurrentField();
        Pattern pattern = Pattern.compile(current.getType().getRegex(), Pattern.CASE_INSENSITIVE);

        return ariUtil.startMohAsync(session.getBridgeId())
                .chain(() -> transcribe(state.getRecording()))
                .flatMap(text -> {
                    session.getFieldHandlingState().setTranscript(text);

                    if (text == null || text.isBlank()) {
                        return Uni.createFrom().voidItem();
                    }

                    Matcher matcher = pattern.matcher(text);
                    state.setInputMatched(false);


                    if (matcher.find()) {
                        state.setInputMatched(true);
                        return fieldResponseService.createFieldResponse(new FieldResponseCreationRequest(current.getId(), session.getChannelName(), text));
                    }

                    return Uni.createFrom().voidItem();
                }).replaceWithVoid();
    }

    @Override
    protected Uni<Void> handleRetryAsync(CallSession session) {
        Uni<Void> endMoh = Uni.createFrom().voidItem().eventually(() -> ariUtil.endMohAsync(session.getBridgeId()));

        FieldHandlingState state = session.getFieldHandlingState();
        if (state.isInputMatched()) {
            state.setRetry(false);
            return endMoh;
        }

        final String userTranscriptSpeech = "field-user-transcript";

        return ariUtil.startMohAsync(session.getBridgeId())
                .chain(() -> speechGenerationService.generateSpeech(new CoquiRequest(session.getFieldHandlingState().getTranscript(), userTranscriptSpeech)))
                .invoke(() -> {
                    session.enqueueAudio(new PlaybackItem("we-understood", session.getBridgeId(), ariUtil, activeAudioRegistry));
                    session.enqueueAudio(new PlaybackItem(userTranscriptSpeech, session.getBridgeId(), ariUtil, activeAudioRegistry));

                    state.setRetry(true);
                    session.resetBaseState();
                }).eventually(() -> endMoh);
    }

    @Override
    protected Uni<Void> handleDoneAsync(CallSession session) {
        if (session.fieldsLeft()) {
            logger.debug("More fields left...");
            session.resetBaseState();
            return Uni.createFrom().voidItem();
        }

        if (session.fieldGroupsLeft()) {
            logger.debug("More field groups left...");
            session.resetIndices();
            session.goToFieldGroup();
            return Uni.createFrom().voidItem();
        } else {
            logger.debug("Done with all fields");
            return super.handleDoneAsync(session);
        }
    }
}
