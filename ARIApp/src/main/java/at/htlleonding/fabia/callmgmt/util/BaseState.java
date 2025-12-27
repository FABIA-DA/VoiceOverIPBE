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
     * Should represent a state, where as input is requested
     */
    REQUEST_INPUT,
    /**
     * Should represent a state, where the previously requested input is processed
     */
    PROCESS_INPUT,
    /**
     * Should give some information if the call state is retried
     */
    RETRY,
    DONE
}
