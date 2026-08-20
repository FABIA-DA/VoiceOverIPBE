package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.util.*;
import at.htlleonding.fabia.client.coquibe.CoquiRequest;
import at.htlleonding.fabia.client.coquibe.SpeechGenerationService;
import at.htlleonding.fabia.client.formbe.FieldResponseService;
import at.htlleonding.fabia.client.formbe.dtos.Field;
import at.htlleonding.fabia.client.formbe.dtos.requests.FieldResponseCreationRequest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Arrays;
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
    private final String userTranscriptSpeech = "field-user-transcript";


    @Override
    protected Uni<Void> handleSingleItemAsync(CallSession session) {
        if (!session.fieldGroupValid()) {
            logger.error("No form or field selected");
            session.enqueue("field-group-invalid", "error");
            session.goToGoodbye();
            return Uni.createFrom().voidItem();
        }

        session.addFields(session.getCurrentFieldGroup().getFields());

        if (session.fieldsEmpty()) {
            session.skipCurrentHandler();
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
        return ariUtil.startMohAsync(session.getChannelId())
                .flatMap(v -> Uni.combine().all().unis(mainSpeech, finalDescriptionSpeech).asTuple())
                .invoke(() -> {
                    String[] media = new String[]{"field-intro", fieldSpeech};

                    if (field.getDescription() != null && !field.getDescription().isBlank()) {
                        media = Arrays.copyOf(media, 3);
                        media[2] = fieldDescription;
                    }

                    session.enqueue("field-intro", media);
                }).replaceWithVoid().eventually(() -> ariUtil.endMohAsync(session.getChannelId()));
    }

    @Override
    protected Uni<Void> handleRequestInputAsync(CallSession session) {
        final String typeDescriptionSpeech = "type-description";
        Field field = session.getCurrentField();

        Uni<Void> fieldDescriptionSpeech = Uni.createFrom().voidItem();

        if (field.getType().getDescription() != null && !field.getType().getDescription().isBlank()) {
            fieldDescriptionSpeech = ariUtil.startMohAsync(session.getChannelId())
                    .chain(() -> speechGenerationService.generateSpeech(new CoquiRequest(field.getType().getDescription(), typeDescriptionSpeech)))
                    .invoke(() -> {
                        session.enqueue("field-input-request", "please-consider", typeDescriptionSpeech);
                    }).eventually(() -> ariUtil.endMohAsync(session.getChannelId()));
        }

        return fieldDescriptionSpeech.invoke(() -> {
            session.getFieldHandlingState().setRecording(session.enqueue());
        });
    }

    @Override
    protected Uni<Void> handleProcessInputAsync(CallSession session) {
        FieldHandlingState state = session.getFieldHandlingState();
        if (state.getRecording() == null) {
            logger.error("Tried to handle an input without a recording");
            session.enqueue("field-recording-null", "error");
            session.goToGoodbye();
            return Uni.createFrom().voidItem();
        }

        Field current = session.getCurrentField();
        Pattern pattern = Pattern.compile(current.getType().getRegex(), Pattern.CASE_INSENSITIVE);

        return ariUtil.startMohAsync(session.getChannelId())
                .chain(() -> transcribe(state.getRecording()))
                .flatMap(text -> {
                    state.setTranscript(text);

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
                }).replaceWithVoid()
                .chain(() -> speechGenerationService.generateSpeech(new CoquiRequest(session.getFieldHandlingState().getTranscript(), userTranscriptSpeech)))
                .invoke(() -> {
                    if (!state.isInputMatched()) {
                        return;
                    }

                    session.enqueue("field-input-check", "please-check", "i-heard", userTranscriptSpeech, "input-ok");
                    state.setCorrectnessRecording(session.enqueue());
                })
                .eventually(() -> ariUtil.endMohAsync(session.getChannelId()));
    }

    @Override
    protected Uni<Void> handleCheckCorrectnessAsync(CallSession session) {
        FieldHandlingState state = session.getFieldHandlingState();
        if (!state.isInputMatched()) {
            return Uni.createFrom().voidItem();
        }

        if (state.getCorrectnessRecording() == null) {
            logger.error("Tried to check field input correctness without recording");
            session.enqueue("field-check-input-null", "error");
            session.goToGoodbye();
            return Uni.createFrom().voidItem();
        }

        return ariUtil.startMohAsync(session.getChannelId())
                .chain(() -> transcribe(state.getCorrectnessRecording()))
                .invoke(text -> {
                    state.setRetry(!text.toLowerCase().contains("ja"));
                })
                .replaceWithVoid()
                .eventually(() -> ariUtil.endMohAsync(session.getChannelId()));
    }

    @Override
    protected Uni<Void> handleRetryAsync(CallSession session) {

        FieldHandlingState state = session.getFieldHandlingState();
        if (state.isInputMatched()) {
            state.setRetry(false);
            return Uni.createFrom().voidItem();
        }

        state.setRetry(true);
        session.resetBaseState();

        String transcript = session.getFieldHandlingState().getTranscript();
        if (transcript == null || transcript.isBlank()) {
            session.enqueue("field-check-transcription-empty", "could-not-understand");
            return Uni.createFrom().voidItem();
        }

        return ariUtil.startMohAsync(session.getChannelId())
                .chain(() -> speechGenerationService.generateSpeech(new CoquiRequest(session.getFieldHandlingState().getTranscript(), userTranscriptSpeech)))
                .invoke(() -> {
                    session.enqueue("field-retry", "could-not-understand", "i-heard", userTranscriptSpeech);
                }).eventually(() -> ariUtil.endMohAsync(session.getChannelId()));
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

            return Uni.createFrom().voidItem().invoke(() -> {
                session.enqueue("fill-out-finished", "action-will-follow");
            }).chain(() -> super.handleDoneAsync(session));
        }
    }
}
