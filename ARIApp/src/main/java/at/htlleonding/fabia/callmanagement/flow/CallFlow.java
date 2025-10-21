package at.htlleonding.fabia.callmanagement.flow;

import at.htlleonding.fabia.callmanagement.CallSession;
import at.htlleonding.fabia.callmanagement.CallState;

import java.util.Optional;

public class CallFlow implements ConversationFlow{
    private CallState currentState;

    @Override
    public CallState getCurrentState() {
        return currentState;
    }

    @Override
    public Optional<CallState> next(CallSession callSession) {
        if(isDone()){
            return Optional.empty();
        }

        if(callSession.getSelectedGroup() == null){
            return Optional.of(CallState.EnterGroup);
        }
        else if (callSession.getSelectedForm() == null){
            return Optional.of(CallState.EnterForm);
        }
        else if (callSession.getCurrentFieldGroupIdx() == null){
            return Optional.of(CallState.FormIntro);
        }
        else if (callSession.getCurrentSingleChoiceFieldIdx() == null){

        }

        return Optional.of(CallState.Error);
    }

    @Override
    public boolean isDone() {
        return false;
    }
}
