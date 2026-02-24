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

        if (session.fieldGroupsEmpty()) {
            session.enqueueAudio(new PlaybackItem("form-empty", session.getChannelId(), ariUtil, activeAudioRegistry));
            session.goToGoodbye();
            return;
        }

        session.tryIncreaseFieldGroupIdx();

        session.enqueueAudio(new PlaybackItem("field-group-info", session.getChannelId(), ariUtil, activeAudioRegistry));

        final String fieldGroupsSpeech = "field-groups-speech";

        List<FieldGroup> fieldGroups = session.getSelectedForm().getFieldGroups();
        String names = Util.ConcatItems(fieldGroups, FieldGroup::getName);


        CoquiRequest request = new CoquiRequest(names, fieldGroupsSpeech);

        speechGenerationService.generateSpeech(request).await().indefinitely();
        session.enqueueAudio(new PlaybackItem("field-group-preamble", session.getChannelId(), ariUtil, activeAudioRegistry));
        session.enqueueAudio(new PlaybackItem(fieldGroupsSpeech, session.getChannelId(), ariUtil, activeAudioRegistry));
    }

    @Override
    protected void handleSingleItem(CallSession session) {
        if (session.currentFieldGroupInBounds()) {
            final String fieldGroupSpeech = "field-group-speech";

            FieldGroup fieldGroup = session.getCurrentFieldGroup();
            session.getUsedFields().clear();

            CoquiRequest request = new CoquiRequest(fieldGroup.getName(), fieldGroupSpeech);
            speechGenerationService.generateSpeech(request).await().indefinitely();
            session.enqueueAudio(new PlaybackItem("next-field-group", session.getChannelId(), ariUtil, activeAudioRegistry));
            session.enqueueAudio(new PlaybackItem(fieldGroupSpeech, session.getChannelId(), ariUtil, activeAudioRegistry));
        } else {
            session.enqueueAudio(new PlaybackItem("field-group-error", session.getChannelId(), ariUtil, activeAudioRegistry));
        }
    }
}
