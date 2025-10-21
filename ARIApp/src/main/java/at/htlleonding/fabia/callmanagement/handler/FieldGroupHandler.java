package at.htlleonding.fabia.callmanagement.handler;

import at.htlleonding.fabia.callmanagement.CallSession;
import at.htlleonding.fabia.callmanagement.CallState;
import at.htlleonding.fabia.callmanagement.HandledState;

@HandledState(CallState.FieldGroupProcessing)
public final class FieldGroupHandler implements CallStateHandler {
    @Override
    public void handle(CallSession session) {
        if(session.getSelectedForm() == null){
            throw new IllegalStateException("No form was selected");
        }

        try{

        }
        catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
