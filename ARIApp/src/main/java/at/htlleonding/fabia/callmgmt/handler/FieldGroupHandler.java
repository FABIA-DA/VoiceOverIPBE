package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.audiomgmt.PlaybackItem;
import at.htlleonding.fabia.callmgmt.util.CallState;
import at.htlleonding.fabia.callmgmt.util.Util;
import at.htlleonding.fabia.callmgmt.util.HandledState;
import at.htlleonding.fabia.client.coquibe.SpeechGenerationClient;
import at.htlleonding.fabia.client.formbe.dtos.FieldGroup;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

@HandledState(CallState.FIELD_GROUP)
public final class FieldGroupHandler extends StateHandler {
    public static final FieldGroupHandler INSTANCE = new FieldGroupHandler();

    private FieldGroupHandler() {
    }

    @Override
    protected void handleInfo(CallSession session) throws IOException {
        if (session.getSelectedForm() == null) {
            throw new IllegalStateException("No form was selected");
        }

        if (session.getSelectedForm().getFieldGroups().isEmpty()) {
            session.enqueueAudio(new PlaybackItem("form_empty", session.getChannelId()));
            return;
        }

        if (session.getCurrentFieldGroupIdx() == null) {
            session.tryIncreaseFieldGroupIdx();
            session.enqueueAudio(new PlaybackItem("field_group_info", session.getChannelId()));

            final String fieldGroupsSpeech = "field-groups-speech";

            List<FieldGroup> fieldGroups = session.getSelectedForm().getFieldGroups();
            String names = Util.ConcatItems(fieldGroups, FieldGroup::getName);

            String speech = MessageFormat.format("Es sind folgende Feldgruppen in diesem Formular: {0}", names);

            SpeechGenerationClient.getClient().generateSpeech(speech, fieldGroupsSpeech);

            session.enqueueAudio(new PlaybackItem(fieldGroupsSpeech, session.getChannelId()));
        }
    }

    @Override
    protected void handleSingleItem(CallSession session) throws IOException {
        if (session.currentFieldGroupInBounds()) {
            final String fieldGroupSpeech = "field-group-speech";

            FieldGroup fieldGroup = session.getCurrentFieldGroup();
            String speech = MessageFormat.format("Es folgen Felder für die Feldgruppe: {0}", fieldGroup.getName());

            SpeechGenerationClient.getClient().generateSpeech(speech, fieldGroupSpeech);

            session.enqueueAudio(new PlaybackItem(fieldGroupSpeech, session.getChannelId()));
        } else {
            session.enqueueAudio(new PlaybackItem("field_group_error", session.getChannelId()));
        }
    }
}
