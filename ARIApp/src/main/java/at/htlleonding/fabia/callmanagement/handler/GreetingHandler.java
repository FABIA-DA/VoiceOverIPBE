package at.htlleonding.fabia.callmanagement.handler;

import at.htlleonding.fabia.callmanagement.*;

@HandledState(CallState.Greeting)
public final class GreetingHandler implements CallStateHandler {
    @Override
    public void handle(CallSession session) {
        session.enqueueAudio(new PlaybackItem("greeting", session.getChannelId()));
        System.out.println("Greeted");
        session.setState(CallState.Group);
        session.nextAudioOrStep();
    }
}