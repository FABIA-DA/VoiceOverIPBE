package at.htlleonding.fabia.callmanagement.handler;

import at.htlleonding.fabia.callmanagement.CallSession;
import at.htlleonding.fabia.callmanagement.CallState;
import at.htlleonding.fabia.callmanagement.CallUtil;
import at.htlleonding.fabia.callmanagement.HandledState;
import at.htlleonding.fabia.client.coquibe.SpeechGenerationClient;
import at.htlleonding.fabia.client.formbe.dtos.Form;

import java.util.List;

@HandledState(CallState.EnterForm)
public final class EnterFormHandler implements CallStateHandler {
    @Override
    public void handle(CallSession session) {
        final String formsSpeechName = "form-names";

        if(session.getSelectedGroup() == null){
            throw new IllegalStateException("No group was selected");
        }

        try{
            List<Form> forms = session.getSelectedGroup().getForms();
            String names = CallUtil.ConcatItems(forms, Form::getName);

            SpeechGenerationClient.getClient().generateSpeech(names, formsSpeechName);

            CallUtil.HandleForms(session, formsSpeechName);
        }
        catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
