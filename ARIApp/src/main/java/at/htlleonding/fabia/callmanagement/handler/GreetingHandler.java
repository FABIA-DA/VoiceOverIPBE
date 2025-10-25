package at.htlleonding.fabia.callmanagement.handler;

import at.htlleonding.fabia.callmanagement.*;

@HandledState(CallState.GREETING)
public final class GreetingHandler extends StateHandler {
    @Override
    protected void handleInfo(CallSession session) {
        session.enqueueAudio(new PlaybackItem("greeting", session.getChannelId()));
        System.out.println("Greeted");
    }
}