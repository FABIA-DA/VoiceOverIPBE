package at.htlleonding.fabia.callmanagement.handler;

import at.htlleonding.fabia.callmanagement.*;

@HandledState(CallState.Greeting)
public final class GreetingHandler extends StateHandler {
    @Override
    protected CallState nextState() {
        return CallState.Group;
    }

    @Override
    protected void handleInfo(CallSession session) {
        session.enqueueAudio(new PlaybackItem("greeting", session.getChannelId()));
        System.out.println("Greeted");
    }
}