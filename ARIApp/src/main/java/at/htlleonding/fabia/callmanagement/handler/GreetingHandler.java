package at.htlleonding.fabia.callmanagement.handler;

import at.htlleonding.fabia.callmanagement.CallAudio;
import at.htlleonding.fabia.callmanagement.CallSession;
import at.htlleonding.fabia.callmanagement.CallState;
import at.htlleonding.fabia.callmanagement.HandledState;

@HandledState(CallState.Greeting)
public final class GreetingHandler implements CallStateHandler {
    @Override
    public void handle(CallSession session) {
        session.getAudioQueue().add("greeting");
        CallAudio.playNext(session);
    }
}