package at.htlleonding.fabia.callmanagement;

public final class StateSequence {
    public static BaseState advanceBaseState(BaseState current) {
        BaseState next = null;
        switch (current) {
            case START -> next = BaseState.INFO;
            case INFO -> next = BaseState.LIST;
            case LIST -> next = BaseState.REQUEST_INPUT;
            case REQUEST_INPUT -> next = BaseState.PROCESS_INPUT;
            case PROCESS_INPUT -> next = BaseState.RETRY;
            case RETRY ->  next = BaseState.CONFIRM;
            case CONFIRM -> next = BaseState.DECIDE;
            case DECIDE -> next = BaseState.DONE;
        }
        return next;
    }

    public static BaseState getFirstBaseState(){
        return BaseState.START;
    }

    public static BaseState getInputState(){
        return BaseState.REQUEST_INPUT;
    }

    public static boolean baseStateIsDone(BaseState current){
        return current.equals(BaseState.DONE);
    }

    public static CallState advanceCallState(CallState current){
        CallState next = null;
        switch (current) {
            case CallState.INIT -> next = CallState.GREETING;
            case CallState.GREETING -> next = CallState.GROUP;
            case CallState.GROUP -> next = CallState.FORM;
            case CallState.FORM -> next = CallState.FIELD_GROUP;
            case CallState.FIELD_GROUP -> next = CallState.SINGLE_CHOICE_FIELD;
            case CallState.SINGLE_CHOICE_FIELD -> next = CallState.FIELD;
            case CallState.FIELD -> next = CallState.GOODBYE;
            case CallState.GOODBYE -> {
            }
        }
        return next;
    }

    public static CallState getFirstCallState(){
        return CallState.INIT;
    }
}
