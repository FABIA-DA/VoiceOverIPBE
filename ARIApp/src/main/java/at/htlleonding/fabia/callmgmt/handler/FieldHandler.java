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
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.text.MessageFormat;
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
    protected void handleSingleItem(CallSession session) {
        if (!session.fieldGroupValid()) {
            throw new IllegalStateException("No form or field selected");
        }

        if(session.fieldsEmpty()){
            session.goToFieldGroup();
            return;
        }

        if (!session.getFieldHandlingState().isRetry()) {
            session.tryIncreaseFieldIdx();
        }

        final String fieldSpeech = "field-speech";
        final String fieldDescription = "field-description";

        Field field = session.getCurrentField();
        String speech = MessageFormat.format("Es folgt das Feld mit dem Namen {0}", field.getName());

        CoquiRequest request = new CoquiRequest(speech, fieldSpeech);

        speechGenerationService.generateSpeech(request).await().indefinitely();

        session.enqueueAudio(new PlaybackItem(fieldSpeech, session.getChannelId(), ariUtil, activeAudioRegistry));

        if (field.getDescription() != null && !field.getDescription().isBlank()) {
            speechGenerationService.generateSpeech(new CoquiRequest(field.getDescription(), fieldDescription)).await().indefinitely();
            session.enqueueAudio(new PlaybackItem(fieldDescription, session.getChannelId(), ariUtil, activeAudioRegistry));
        }
    }

    @Override
    protected void handleRequestInput(CallSession session) {
        final String typeDescriptionSpeech = "type-description";
        Field field = session.getCurrentField();

        if (field.getDescription() != null) {
            String speech = MessageFormat.format("Bitte beachten Sie folgendes für die Eingabe: {0}", field.getDescription());
            speechGenerationService.generateSpeech(new CoquiRequest(speech, typeDescriptionSpeech)).await().indefinitely();
            session.enqueueAudio(new PlaybackItem(typeDescriptionSpeech, session.getChannelId(), ariUtil, activeAudioRegistry));
        }

        RecordingItem recording = new RecordingItem(session.getChannelId(), ariUtil, activeAudioRegistry);
        session.getFieldHandlingState().setRecording(recording);
        session.enqueueAudio(recording);
    }

    @Override
    protected void handleProcessInput(CallSession session) {
        FieldHandlingState state = session.getFieldHandlingState();
        if (state.getRecording() == null) {
            throw new IllegalStateException("Tried to handle an input without a recording");
        }

        String text = transcribe(state.getRecording()).await().indefinitely();

        if (text == null) {
            return;
        }

        Field current = session.getCurrentField();
        Pattern pattern = Pattern.compile(current.getType().getRegex(), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        state.setInputMatched(false);

        if (matcher.find()) {
            fieldResponseService.createFieldResponse(new FieldResponseCreationRequest(current.getId(), session.getChannelName(), text))
                    .log()
                    .await()
                    .indefinitely();
            state.setInputMatched(true);
        }
    }

    @Override
    protected void handleRetry(CallSession session) {
        FieldHandlingState state = session.getFieldHandlingState();
        if (state.isInputMatched()) {
            state.setRetry(false);
            return;
        }

        session.enqueueAudio(new PlaybackItem("field-input-mismatch", session.getChannelId(), ariUtil, activeAudioRegistry));
        state.setRetry(true);
        session.resetBaseState();
    }

    @Override
    protected void handleDone(CallSession session) {
        if (session.fieldsLeft()) {
            logger.debug("More fields left...");
            session.resetBaseState();
            return;
        }

        if (session.fieldGroupsLeft()) {
            logger.debug("More field groups left...");
            session.resetIndices();
            session.goToFieldGroup();
        } else {
            logger.debug("Done with all fields");
            super.handleDone(session);
        }
    }
}
