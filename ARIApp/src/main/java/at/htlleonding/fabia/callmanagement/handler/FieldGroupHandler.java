package at.htlleonding.fabia.callmanagement.handler;

import at.htlleonding.fabia.callmanagement.CallSession;
import at.htlleonding.fabia.callmanagement.CallState;
import at.htlleonding.fabia.callmanagement.HandledState;
import at.htlleonding.fabia.callmanagement.PlaybackItem;

@HandledState(CallState.FieldGroup)
public final class FieldGroupHandler extends StateHandler {
    @Override
    protected CallState nextState() {
        return null;
    }

    @Override
    protected void handleInfo(CallSession session) {
        if(session.getSelectedForm() == null){
            throw new IllegalStateException("No form was selected");
        }

        session.enqueueAudio(new PlaybackItem("goodbye", session.getChannelId()));    }
}
