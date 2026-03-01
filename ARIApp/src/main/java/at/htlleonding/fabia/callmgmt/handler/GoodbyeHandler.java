package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.CallSession;
import at.htlleonding.fabia.callmgmt.audiomgmt.ActiveAudioRegistry;
import at.htlleonding.fabia.callmgmt.util.AriUtil;
import at.htlleonding.fabia.callmgmt.util.CallState;
import at.htlleonding.fabia.callmgmt.util.HandledState;
import at.htlleonding.fabia.callmgmt.audiomgmt.PlaybackItem;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@HandledState(CallState.GOODBYE)
public final class GoodbyeHandler extends StateHandler {
    @Inject
    AriUtil ariUtil;
    @Inject
    ActiveAudioRegistry activeAudioRegistry;

    @Override
    protected Uni<Void> handleInfoAsync(CallSession session) {
        return Uni.createFrom().voidItem().invoke(() ->
                session.enqueueAudio(new PlaybackItem("fabia-goodbye", session.getBridgeId(), ariUtil, activeAudioRegistry)));
    }

    @Override
    protected Uni<Void> handleDoneAsync(CallSession session) {
        return Uni.createFrom().voidItem().invoke(() -> session.endCall(false));
    }
}
