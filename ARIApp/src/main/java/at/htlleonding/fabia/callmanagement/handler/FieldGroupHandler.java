package at.htlleonding.fabia.callmanagement.handler;

import at.htlleonding.fabia.callmanagement.*;
import at.htlleonding.fabia.client.coquibe.SpeechGenerationClient;
import at.htlleonding.fabia.client.formbe.dtos.FieldGroup;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

@HandledState(CallState.FIELD_GROUP)
public final class FieldGroupHandler extends StateHandler {
    @Override
    protected void handleInfo(CallSession session) throws IOException {
        if (session.getSelectedForm() == null) {
            throw new IllegalStateException("No form was selected");
        }

        session.enqueueAudio(new PlaybackItem("field_group_info", session.getChannelId()));

        if (session.getCurrentFieldGroupIdx() == null) {
            session.setCurrentFieldGroupIdx(0);

            final String fieldGroupsSpeech = "field-groups-speech";

            List<FieldGroup> fieldGroups = session.getSelectedForm().getFieldGroups();
            String names = CallUtil.ConcatItems(fieldGroups, FieldGroup::getName);

            String speech = MessageFormat.format("Es sind folgende Feldgruppen in diesem Formular: {0}", names);

            SpeechGenerationClient.getClient().generateSpeech(speech, fieldGroupsSpeech);

            session.enqueueAudio(new PlaybackItem(fieldGroupsSpeech, session.getChannelId()));
        }

        if (session.getCurrentFieldGroupIdx() >= 0 && session.getCurrentFieldGroupIdx() < session.getSelectedForm().getFieldGroups().size()) {
            final String fieldGroupSpeech = "field-group-speech";

            FieldGroup fieldGroup = session.getSelectedForm().getFieldGroups().get(session.getCurrentFieldGroupIdx());
            String speech = MessageFormat.format("Es folgen Felder für die Feldgruppe: {0}", fieldGroup.getName());

            SpeechGenerationClient.getClient().generateSpeech(speech, fieldGroupSpeech);

            session.enqueueAudio(new PlaybackItem(fieldGroupSpeech, session.getChannelId()));
        } else {
            session.enqueueAudio(new PlaybackItem("field_group_error", session.getChannelId()));
        }
    }
}
