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
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.rest.client.inject.RestClient;

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
    protected Uni<Void> handleInfoAsync(CallSession session) {
        if (session.getSelectedForm() == null) {
            throw new IllegalStateException("No form was selected");
        }

        if (session.fieldGroupsEmpty()) {
            session.enqueueAudio(
                    new PlaybackItem("form-empty",
                            session.getBridgeId(),
                            ariUtil,
                            activeAudioRegistry));
            session.goToGoodbye();
            return Uni.createFrom().voidItem();
        }

        final String fieldGroupsSpeech = "field-groups-speech";

        return ariUtil.startMohAsync(session.getBridgeId())
                .chain(() -> {
                    session.tryIncreaseFieldGroupIdx();

                    List<FieldGroup> fieldGroups = session.getSelectedForm().getFieldGroups();
                    String names = Util.ConcatItems(fieldGroups, FieldGroup::getName);
                    CoquiRequest request = new CoquiRequest(names, fieldGroupsSpeech);

                    return speechGenerationService.generateSpeech(request);
                })
                .invoke(() -> {
                    session.enqueueAudio(
                            new PlaybackItem("field-group-info",
                                    session.getBridgeId(),
                                    ariUtil,
                                    activeAudioRegistry));
                    session.enqueueAudio(
                            new PlaybackItem("field-group-preamble",
                                    session.getBridgeId(),
                                    ariUtil,
                                    activeAudioRegistry));
                    session.enqueueAudio(
                            new PlaybackItem(fieldGroupsSpeech,
                                    session.getBridgeId(),
                                    ariUtil,
                                    activeAudioRegistry));
                })
                .eventually(() -> ariUtil.endMohAsync(session.getBridgeId()));
    }

    @Override
    protected Uni<Void> handleSingleItemAsync(CallSession session) {
        if (!session.currentFieldGroupInBounds()) {
            session.enqueueAudio(
                    new PlaybackItem("field-group-error",
                            session.getBridgeId(),
                            ariUtil,
                            activeAudioRegistry));
            return Uni.createFrom().voidItem();
        }

        final String fieldGroupSpeech = "field-group-speech";

        return ariUtil.startMohAsync(session.getBridgeId())
                .chain(() -> {
                    FieldGroup fieldGroup = session.getCurrentFieldGroup();
                    session.getUsedFields().clear();

                    CoquiRequest request = new CoquiRequest(fieldGroup.getName(), fieldGroupSpeech);
                    return speechGenerationService.generateSpeech(request);
                })
                .invoke(() -> {
                    session.enqueueAudio(
                            new PlaybackItem("next-field-group",
                                    session.getBridgeId(),
                                    ariUtil,
                                    activeAudioRegistry));
                    session.enqueueAudio(
                            new PlaybackItem(fieldGroupSpeech,
                                    session.getBridgeId(),
                                    ariUtil,
                                    activeAudioRegistry));
                })
                .eventually(() -> ariUtil.endMohAsync(session.getBridgeId()));
    }
}
