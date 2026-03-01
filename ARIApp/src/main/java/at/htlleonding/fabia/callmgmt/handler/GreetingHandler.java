package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.audiomgmt.ActiveAudioRegistry;
import at.htlleonding.fabia.callmgmt.audiomgmt.PlaybackItem;
import at.htlleonding.fabia.callmgmt.util.AriUtil;
import at.htlleonding.fabia.callmgmt.util.CallState;
import at.htlleonding.fabia.callmgmt.util.HandledState;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@HandledState(CallState.GREETING)
public final class GreetingHandler extends StateHandler {
    @Inject
    AriUtil ariUtil;
    @Inject
    ActiveAudioRegistry activeAudioRegistry;

    @Override
    protected Uni<Void> handleInfoAsync(CallSession session) {
        return Uni.createFrom().deferred(() -> {
            session.enqueueAudio(new PlaybackItem("greeting", session.getBridgeId(), ariUtil, activeAudioRegistry));
            return Uni.createFrom().voidItem();
        });
    }
}