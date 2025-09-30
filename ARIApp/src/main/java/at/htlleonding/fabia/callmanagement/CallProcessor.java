package at.htlleonding.fabia.callmanagement;

import java.util.Map;

public final class CallProcessor {
    private static final Map<CallState, CallStateHandler> handlers = Map.of(
            CallState.EnteringGroup, new EnteringGroupHandler()
    );

    public static void process(CallSession session) {
        handlers.get(session.getState()).handle(session);
    }
}
