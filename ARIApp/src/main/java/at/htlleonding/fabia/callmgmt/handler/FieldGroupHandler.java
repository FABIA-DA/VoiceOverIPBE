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
    @Inject
    AriUtil ariUtil;
    @Inject
    ActiveAudioRegistry activeAudioRegistry;

    @Override
    protected Uni<Void> handleInfoAsync(CallSession session) {
        if (session.getSelectedForm() == null) {
            logger.error("No Form was selected");
            session.enqueueAudio(new PlaybackItem("error", session.getBridgeId(), ariUtil, activeAudioRegistry));
            session.goToGoodbye();
            return Uni.createFrom().voidItem();
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

        session.tryIncreaseFieldGroupIdx();

        return Uni.createFrom().voidItem();
    }

    @Override
    protected Uni<Void> handleSingleItemAsync(CallSession session) {
        if (session.currentFieldGroupInBounds()) {
            session.getUsedFields().clear();
        }

        return Uni.createFrom().voidItem();
    }
}
