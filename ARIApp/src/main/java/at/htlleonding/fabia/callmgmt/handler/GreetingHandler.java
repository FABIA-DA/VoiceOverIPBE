package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.audiomgmt.PlaybackItem;
import at.htlleonding.fabia.callmgmt.util.CallState;
import at.htlleonding.fabia.callmgmt.util.HandledState;

@HandledState(CallState.GREETING)
public final class GreetingHandler extends StateHandler {
    public static final GreetingHandler INSTANCE = new GreetingHandler();
    private GreetingHandler() {}

    @Override
    protected void handleInfo(CallSession session) {
        session.enqueueAudio(new PlaybackItem("greeting", session.getChannelId()));
    }
}