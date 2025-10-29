package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.CallSession;
import at.htlleonding.fabia.callmgmt.util.CallState;
import at.htlleonding.fabia.callmgmt.util.HandledState;
import at.htlleonding.fabia.callmgmt.audiomgmt.PlaybackItem;

import java.io.IOException;

@HandledState(CallState.GOODBYE)
public final class GoodbyeHandler extends StateHandler {
    public static final GoodbyeHandler INSTANCE = new GoodbyeHandler();

    private GoodbyeHandler(){
    }

    @Override
    protected void handleInfo(CallSession session) throws IOException {
        session.enqueueAudio(new PlaybackItem("goodbye", session.getChannelId()));
    }

    @Override
    protected void handleDone(CallSession session) {
        session.hangup();
    }
}
