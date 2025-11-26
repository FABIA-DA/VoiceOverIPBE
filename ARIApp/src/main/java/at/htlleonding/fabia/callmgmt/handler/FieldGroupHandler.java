package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.audiomgmt.ActiveAudioRegistry;
import at.htlleonding.fabia.callmgmt.audiomgmt.PlaybackItem;
import at.htlleonding.fabia.callmgmt.util.AriUtil;
import at.htlleonding.fabia.callmgmt.util.CallState;
import at.htlleonding.fabia.callmgmt.util.Util;
import at.htlleonding.fabia.callmgmt.util.HandledState;
import at.htlleonding.fabia.client.coquibe.CoquiRequest;
import at.htlleonding.fabia.client.coquibe.SpeechGenerationService;
import at.htlleonding.fabia.client.formbe.dtos.FieldGroup;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.text.MessageFormat;
import java.util.List;

@Singleton
@HandledState(CallState.FIELD_GROUP)
public final class FieldGroupHandler extends StateHandler {
    @RestClient
    SpeechGenerationService speechGenerationService;
    @Inject
    AriUtil ariUtil;
    @Inject
    ActiveAudioRegistry activeAudioRegistry;

    @Override
    protected void handleInfo(CallSession session) {
        if (session.getSelectedForm() == null) {
            throw new IllegalStateException("No form was selected");
        }

        if (session.getSelectedForm().getFieldGroups().isEmpty()) {
            session.enqueueAudio(new PlaybackItem("form_empty", session.getChannelId(), ariUtil, activeAudioRegistry));
            return;
        }

        if (session.getCurrentFieldGroupIdx() == null) {
            session.tryIncreaseFieldGroupIdx();
            session.enqueueAudio(new PlaybackItem("field_group_info", session.getChannelId(), ariUtil, activeAudioRegistry));

            final String fieldGroupsSpeech = "field-groups-speech";

            List<FieldGroup> fieldGroups = session.getSelectedForm().getFieldGroups();
            String names = Util.ConcatItems(fieldGroups, FieldGroup::getName);

            String speech = MessageFormat.format("Es sind folgende Feldgruppen in diesem Formular: {0}", names);

            CoquiRequest request = new CoquiRequest(speech, fieldGroupsSpeech);

            speechGenerationService.generateSpeech(request).await().indefinitely();
            session.enqueueAudio(new PlaybackItem(fieldGroupsSpeech, session.getChannelId(), ariUtil, activeAudioRegistry));
        }
    }

    @Override
    protected void handleSingleItem(CallSession session) {
        if (session.currentFieldGroupInBounds()) {
            final String fieldGroupSpeech = "field-group-speech";

            FieldGroup fieldGroup = session.getCurrentFieldGroup();
            String speech = MessageFormat.format("Es folgen Felder für die Feldgruppe: {0}", fieldGroup.getName());

            CoquiRequest request = new CoquiRequest(speech, fieldGroupSpeech);
            speechGenerationService.generateSpeech(request).await().indefinitely();
            session.enqueueAudio(new PlaybackItem(fieldGroupSpeech, session.getChannelId(), ariUtil, activeAudioRegistry));
        } else {
            session.enqueueAudio(new PlaybackItem("field_group_error", session.getChannelId(), ariUtil, activeAudioRegistry));
        }
    }
}
