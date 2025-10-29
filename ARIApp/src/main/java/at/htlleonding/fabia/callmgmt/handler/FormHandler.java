package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.audiomgmt.PlaybackItem;
import at.htlleonding.fabia.callmgmt.audiomgmt.RecordingItem;
import at.htlleonding.fabia.callmgmt.util.CallState;
import at.htlleonding.fabia.callmgmt.util.Util;
import at.htlleonding.fabia.callmgmt.util.HandledState;
import at.htlleonding.fabia.client.coquibe.SpeechGenerationClient;
import at.htlleonding.fabia.client.formbe.dtos.Form;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@HandledState(CallState.FORM)
public final class FormHandler extends StateHandler {
    public static final FormHandler INSTANCE = new FormHandler();
    private FormHandler(){
    }

    @Override
    protected void handleList(CallSession session) throws IOException {
        final String formsSpeechName = "from-names";

        if (session.getSelectedGroup() == null) {
            throw new IllegalStateException("No group for form intro was selected");
        }

        List<Form> forms = session.getSelectedGroup().getForms();
        String names = Util.ConcatItems(forms, Form::getName);

        SpeechGenerationClient.getClient().generateSpeech("Wir haben diese Formulare verfügbar: " + names, formsSpeechName);

        session.enqueueAudio(new PlaybackItem(formsSpeechName, session.getChannelId()));
    }

    @Override
    protected void handleRequestInput(CallSession session) {
        session.enqueueAudio(new PlaybackItem("form_input_request", session.getChannelId()));
        RecordingItem recordingItem = new RecordingItem(session.getChannelId());
        session.getFormHandlingState().setRecording(recordingItem);
        session.enqueueAudio(recordingItem);
    }

    @Override
    protected void handleProcessInput(CallSession session) throws IOException {
        RecordingItem recording =  session.getFormHandlingState().getRecording();
        if (recording == null) {
            throw new IllegalStateException("No recording happened before input processing");
        }

        String text = transcribe(recording);

        for (Form form : session.getSelectedGroup().getForms()) {
            Pattern pattern = Pattern.compile(form.getName(), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                session.setSelectedForm(form);
                break;
            }
        }
    }

    @Override
    protected void handleRetry(CallSession session) {
        if(session.getSelectedForm() != null){
            return;
        }

        session.enqueueAudio(new PlaybackItem("form_not_found", session.getChannelId()));
        session.resetBaseState();
    }
}
