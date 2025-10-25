package at.htlleonding.fabia.callmanagement.handler;

import at.htlleonding.fabia.callmanagement.*;
import at.htlleonding.fabia.client.coquibe.SpeechGenerationClient;
import at.htlleonding.fabia.client.formbe.dtos.Form;
import at.htlleonding.fabia.client.whisperbe.TranscriptionClient;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@HandledState(CallState.FORM)
public final class FormHandler extends StateHandler {
    private RecordingItem recordingItem = null;

    @Override
    protected void handleList(CallSession session) throws IOException {
        final String formsSpeechName = "field-names";

        if (session.getSelectedGroup() == null) {
            throw new IllegalStateException("No group for form intro was selected");
        }

        List<Form> forms = session.getSelectedGroup().getForms();
        String names = CallUtil.ConcatItems(forms, Form::getName);

        SpeechGenerationClient.getClient().generateSpeech("Wir haben diese Formulare verfügbar: " + names, formsSpeechName);

        //session.enqueueAudio(new PlaybackItem("form_list", session.getChannelId()));
        session.enqueueAudio(new PlaybackItem(formsSpeechName, session.getChannelId()));
    }

    @Override
    protected void handleRequestInput(CallSession session) {
        session.enqueueAudio(new PlaybackItem("form_input_request", session.getChannelId()));
        recordingItem = new RecordingItem(session.getChannelId());
        session.enqueueAudio(recordingItem);
    }

    @Override
    protected void handleProcessInput(CallSession session) throws IOException {
        if (recordingItem == null) {
            return;
        }

        String filePath = "/app/recordings/" + recordingItem.getName() + ".wav";

        String text = TranscriptionClient.getClient().transcribe(filePath);

        if (text == null) {
            return;
        }

        for (Form form : session.getSelectedGroup().getForms()) {
            System.out.println("Going through form");
            Pattern pattern = Pattern.compile(form.getName(), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                System.out.println("Selected form: " + form.getName());
                session.setSelectedForm(form);
                break;
            }
        }
    }
}
