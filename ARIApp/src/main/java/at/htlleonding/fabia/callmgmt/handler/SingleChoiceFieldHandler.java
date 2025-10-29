package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.audiomgmt.PlaybackItem;
import at.htlleonding.fabia.callmgmt.audiomgmt.RecordingItem;
import at.htlleonding.fabia.callmgmt.util.CallState;
import at.htlleonding.fabia.callmgmt.util.Util;
import at.htlleonding.fabia.callmgmt.util.HandledState;
import at.htlleonding.fabia.client.coquibe.SpeechGenerationClient;
import at.htlleonding.fabia.client.formbe.OptionResponseClient;
import at.htlleonding.fabia.client.formbe.dtos.Option;
import at.htlleonding.fabia.client.formbe.dtos.SingleChoiceField;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@HandledState(CallState.SINGLE_CHOICE_FIELD)
public final class SingleChoiceFieldHandler extends StateHandler {
    public static final SingleChoiceFieldHandler INSTANCE = new SingleChoiceFieldHandler();
    private SingleChoiceFieldHandler() {}

    @Override
    protected void handleSingleItem(CallSession session) throws IOException {
        if (!session.fieldGroupValid()) {
            throw new IllegalStateException("Did not select a form or field group");
        }

        final String singleChoiceFieldSpeech = "single-choice-field-speech";

        if (!session.getSingleChoiceFieldHandlingState().isRetry()) {
            session.tryIncreaseSingleChoiceFieldIdx();
        }

        if (!session.currentSingleChoiceFieldInBounds()) {
            throw new IllegalStateException("Single Choice Field Idx not in bounds");
        }

        SingleChoiceField field = session.getCurrentSingleChoiceField();
        String options = Util.ConcatItems(field.getOptions(), Option::getName);
        String speech = MessageFormat.format("Für das Feld mit dem Namen {0} gibt es folgende Optionen: {1}", field.getName(), options);

        SpeechGenerationClient.getClient().generateSpeech(speech, singleChoiceFieldSpeech);

        session.enqueueAudio(new PlaybackItem(singleChoiceFieldSpeech, session.getChannelId()));
    }

    @Override
    protected void handleRequestInput(CallSession session) {
        session.enqueueAudio(new PlaybackItem("single_choice_field_input_request", session.getChannelId()));
        RecordingItem recording = new RecordingItem(session.getChannelId());
        session.getSingleChoiceFieldHandlingState().setRecording(recording);
        session.enqueueAudio(recording);
    }

    @Override
    protected void handleProcessInput(CallSession session) throws IOException {
        RecordingItem recording = session.getSingleChoiceFieldHandlingState().getRecording();
        if (recording == null) {
            throw new IllegalStateException("No recording happened before input processing");
        }

        String text = transcribe(recording);

        for (Option option : session.getCurrentSingleChoiceField().getOptions()) {
            Pattern pattern = Pattern.compile(option.getName(), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                session.getSingleChoiceFieldHandlingState().setOptionMatch(true);
                OptionResponseClient.getClient().createOptionResponse(option.getId(), session.getChannelName());
                session.addFields(option.getFields());
                break;
            }
        }
    }

    @Override
    protected void handleRetry(CallSession session) {
        if (session.getSingleChoiceFieldHandlingState().isOptionMatch()) {
            session.getSingleChoiceFieldHandlingState().setRetry(false);
            return;
        }

        session.enqueueAudio(new PlaybackItem("option_not_found", session.getChannelId()));
        session.getSingleChoiceFieldHandlingState().setRetry(true);
        session.resetBaseState();
    }

    @Override
    protected void handleDone(CallSession session) {
        if (session.singleChoiceFieldsLeft()) {
            session.resetBaseState();
        } else {
            super.handleDone(session);
        }
    }
}
