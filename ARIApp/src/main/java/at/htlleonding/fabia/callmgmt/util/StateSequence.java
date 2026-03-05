package at.htlleonding.fabia.callmgmt.util;

public final class StateSequence {
    /**
     * Advances the base state in a specific order.
     *
     * @param current The current base
     * @return The new base state
     */
    public static BaseState advanceBaseState(BaseState current) {
        BaseState next;
        next = switch (current) {
            case START -> BaseState.INFO;
            case INFO -> BaseState.LIST;
            case LIST -> BaseState.SINGLE_ITEM;
            case SINGLE_ITEM -> BaseState.REQUEST_INPUT;
            case REQUEST_INPUT -> BaseState.PROCESS_INPUT;
            case PROCESS_INPUT -> BaseState.CHECK_CORRECTNESS;
            case CHECK_CORRECTNESS -> BaseState.RETRY;
            case RETRY, DONE -> BaseState.DONE;
        };
        return next;
    }

    public static BaseState getFirstBaseState() {
        return BaseState.START;
    }

    /**
     * Advances the call state in a specific order.
     *
     * @param current The current call state
     * @return The next call state
     */
    public static CallState advanceCallState(CallState current) {
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

    public static CallState getFirstCallState() {
        return CallState.INIT;
    }
}
