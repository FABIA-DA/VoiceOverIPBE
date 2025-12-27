package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.CallSession;
import at.htlleonding.fabia.callmgmt.audiomgmt.ActiveAudioRegistry;
import at.htlleonding.fabia.callmgmt.util.AriUtil;
import at.htlleonding.fabia.callmgmt.util.CallState;
import at.htlleonding.fabia.callmgmt.util.HandledState;
import at.htlleonding.fabia.callmgmt.audiomgmt.PlaybackItem;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;

@Singleton
@HandledState(CallState.GOODBYE)
public final class GoodbyeHandler extends StateHandler {
    @Inject
    AriUtil ariUtil;
    @Inject
    ActiveAudioRegistry activeAudioRegistry;

    @Override
    protected void handleInfo(CallSession session) throws IOException {
        session.enqueueAudio(new PlaybackItem("fabia-goodbye", session.getChannelId(), ariUtil, activeAudioRegistry));
    }

    @Override
    protected void handleDone(CallSession session) {
        session.endCall(false);
    }
}
