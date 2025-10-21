package at.htlleonding.fabia.callmanagement.handler;

import at.htlleonding.fabia.callmanagement.*;
import at.htlleonding.fabia.client.coquibe.SpeechGenerationClient;
import at.htlleonding.fabia.client.formbe.dtos.Field;
import at.htlleonding.fabia.client.formbe.dtos.FieldGroup;
import at.htlleonding.fabia.client.formbe.dtos.SingleChoiceField;

import java.util.List;

@HandledState(CallState.FormIntro)
public final class FormIntroHandler implements CallStateHandler {
    @Override
    public void handle(CallSession session) {
        final String fieldNamesFile = "field-names";

        if(session.getSelectedForm() == null){
            throw new IllegalStateException("No form for intro was selected");
        }

        try{
            List<FieldGroup> fieldGroups = session.getSelectedForm().getFieldGroups();
            String names = CallUtil.ConcatItems(fieldGroups, fg -> {
                String fieldNames = CallUtil.ConcatItems(fg.getFields(), Field::getName);
                String singleChoiceFieldNames = CallUtil.ConcatItems(fg.getSingleChoiceFields(), SingleChoiceField::getName);

                return singleChoiceFieldNames + ", " + fieldNames;
            });

            SpeechGenerationClient.getClient().generateSpeech(names, fieldNamesFile);

            CallUtil.FormIntro(session, fieldNamesFile);

            session.setState(CallState.FieldGroupProcessing);
            CallProcessor.process(session);
        }
        catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
