package at.htlleonding.fabia.callmgmt.util;

public enum BaseState {
    START,
    /**
     * Aims to provide some info about the current call state
     */
    INFO,
    /**
     * Provides information about list of items
     */
    LIST,
    /**
     * Provides information about a single item
     */
    SINGLE_ITEM,
    /**
     * Should represent a state, where input is requested
     */
    REQUEST_INPUT,
    /**
     * Should represent a state, where the previously requested input is processed and a check for correctness is requested
     */
    PROCESS_INPUT,
    /**
     * Should handle the correctness input from the previous state
     */
    CHECK_CORRECTNESS,
    /**
     * Should give some information if the call state is retried
     */
    RETRY,
    DONE
}
