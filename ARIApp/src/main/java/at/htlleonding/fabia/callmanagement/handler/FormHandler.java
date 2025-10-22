package at.htlleonding.fabia.callmanagement.handler;

import at.htlleonding.fabia.callmanagement.*;
import at.htlleonding.fabia.client.coquibe.SpeechGenerationClient;
import at.htlleonding.fabia.client.formbe.dtos.Form;
import at.htlleonding.fabia.client.whisperbe.TranscriptionClient;

import java.io.IOException;
import java.util.List;

@HandledState(CallState.Form)
public final class FormHandler implements CallStateHandler {
    private boolean generated = false;
    private RecordingItem recordingItem = null;

    @Override
    public void handle(CallSession session) {
        switch(session.getCurrentBaseState()){
            case BaseState.List -> {
                final String formsSpeechName = "field-names";

                if(session.getSelectedForm() == null){
                    throw new IllegalStateException("No form for intro was selected");
                }

                try{
                    if(!generated){
                        List<Form> forms = session.getSelectedGroup().getForms();
                        String names = CallUtil.ConcatItems(forms, Form::getName);

                        SpeechGenerationClient.getClient().generateSpeech(names, formsSpeechName);
                        generated = true;
                    }

                    session.enqueueAudio(new PlaybackItem("form_list", session.getChannelId()));
                    session.enqueueAudio(new PlaybackItem(formsSpeechName, session.getChannelId()));
                }
                catch(Exception ex){
                    throw new RuntimeException(ex);
                }
            }
            case BaseState.RequestInput -> {
                session.enqueueAudio(new PlaybackItem("form_input_request", session.getChannelId()));
                recordingItem = new RecordingItem(session.getChannelId());
                session.enqueueAudio(recordingItem);
            }
            case BaseState.ProcessInput -> {
                if(recordingItem == null){
                    return;
                }

                try{
                    String text = TranscriptionClient.getClient().transcribe(recordingItem.getName());

                    if(text == null){
                        return;
                    }

                    for(Form form : session.getSelectedGroup().getForms()){
                        if(form.getName().trim().equalsIgnoreCase(text.trim())){
                            session.setSelectedForm(form);
                            break;
                        }
                    }

                    session.setState(CallState.FieldGroup);
                }
                catch(IOException ex){
                    throw new RuntimeException(ex);
                }
            }
        }

        session.nextAudioOrStep();
    }
}
