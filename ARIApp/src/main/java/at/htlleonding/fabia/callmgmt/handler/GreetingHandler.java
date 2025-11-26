package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.audiomgmt.ActiveAudioRegistry;
import at.htlleonding.fabia.callmgmt.audiomgmt.PlaybackItem;
import at.htlleonding.fabia.callmgmt.util.AriUtil;
import at.htlleonding.fabia.callmgmt.util.CallState;
import at.htlleonding.fabia.callmgmt.util.HandledState;
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
    protected void handleInfo(CallSession session) {
        session.enqueueAudio(new PlaybackItem("greeting", session.getChannelId(), ariUtil, activeAudioRegistry));
    }
}