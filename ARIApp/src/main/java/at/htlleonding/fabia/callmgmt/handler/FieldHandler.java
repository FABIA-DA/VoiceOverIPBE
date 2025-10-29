package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.audiomgmt.PlaybackItem;
import at.htlleonding.fabia.callmgmt.audiomgmt.RecordingItem;
import at.htlleonding.fabia.callmgmt.util.CallState;
import at.htlleonding.fabia.callmgmt.util.FieldHandlingState;
import at.htlleonding.fabia.callmgmt.util.HandledState;
import at.htlleonding.fabia.client.coquibe.SpeechGenerationClient;
import at.htlleonding.fabia.client.formbe.FieldResponseClient;
import at.htlleonding.fabia.client.formbe.dtos.Field;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@HandledState(CallState.FIELD)
public final class FieldHandler extends StateHandler {
    public static final FieldHandler INSTANCE = new FieldHandler();

    private FieldHandler() {
    }

    @Override
    protected void handleSingleItem(CallSession session) throws IOException {
        if (!session.fieldGroupValid()) {
            throw new IllegalStateException("No form or field selected");
        }

        if (!session.getFieldHandlingState().isRetry()) {
            session.tryIncreaseFieldIdx();
        }

        final String fieldSpeech = "field-speech";
        final String fieldDescription = "field-description";

        Field field = session.getCurrentField();
        String speech = MessageFormat.format("Es folgt das Feld mit dem Namen {0}", field.getName());

        SpeechGenerationClient.getClient().generateSpeech(speech, fieldSpeech);

        session.enqueueAudio(new PlaybackItem(fieldSpeech, session.getChannelId()));

        if (field.getDescription() != null && !field.getDescription().isBlank()) {
            SpeechGenerationClient.getClient().generateSpeech(field.getDescription(), fieldDescription);

            session.enqueueAudio(new PlaybackItem(fieldDescription, session.getChannelId()));
        }
    }

    @Override
    protected void handleRequestInput(CallSession session) throws IOException {
        final String typeDescriptionSpeech = "type-description";
        Field field = session.getCurrentField();

        if (field.getDescription() != null) {
            String speech = MessageFormat.format("Bitte beachten Sie folgendes für die Eingabe: {0}", field.getDescription());
            SpeechGenerationClient.getClient().generateSpeech(speech, typeDescriptionSpeech);
            session.enqueueAudio(new PlaybackItem(typeDescriptionSpeech, session.getChannelId()));
        }

        RecordingItem recording = new RecordingItem(session.getChannelId());
        session.getFieldHandlingState().setRecording(recording);
        session.enqueueAudio(recording);
    }

    @Override
    protected void handleProcessInput(CallSession session) throws IOException {
        FieldHandlingState state = session.getFieldHandlingState();
        if (state.getRecording() == null) {
            throw new IllegalStateException("Tried to handle an input without a recording");
        }

        String text = transcribe(state.getRecording());

        Field current = session.getCurrentField();
        Pattern pattern = Pattern.compile(current.getType().getRegex(), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        state.setInputMatched(false);

        if (matcher.find()) {
            FieldResponseClient.getClient().createFieldResponse(current.getId(), session.getChannelName(), text);
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

        session.enqueueAudio(new PlaybackItem("field_input_mismatch", session.getChannelId()));
        state.setRetry(true);
        session.resetBaseState();
    }

    @Override
    protected void handleDone(CallSession session) {
        if (session.fieldsLeft()) {
            System.out.println("redo field handling");
            session.resetBaseState();
            return;
        }

        if (session.fieldGroupsLeft()) {
            System.out.println("go to next field group");
            session.resetIndexes();
            session.goToFieldGroup();
        } else {
            System.out.println("finish");
            super.handleDone(session);
        }
    }
}
