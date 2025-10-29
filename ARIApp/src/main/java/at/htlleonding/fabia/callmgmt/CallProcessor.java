package at.htlleonding.fabia.callmgmt;

import at.htlleonding.fabia.callmgmt.handler.StateHandler;
import at.htlleonding.fabia.callmgmt.util.CallState;
import at.htlleonding.fabia.callmgmt.util.HandledState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class CallProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CallProcessor.class);
    private static final Map<CallState, StateHandler> handlerMap = initHandlerMap();

    public static void process(CallSession session) {
        StateHandler handler = handlerMap.get(session.getState());

        if (handler == null) {
            logger.warn("No handler registered for state {}", session.getState());
            return;
        }

        try {
            handler.handle(session);
        } catch (Exception e) {
            logger.error("Error handling state {} for session {}", session.getState(), session.getChannelId(), e);
        }
    }

    private static Map<CallState, StateHandler> initHandlerMap() {
        Map<CallState, StateHandler> map = new EnumMap<>(CallState.class);

        for (Class<?> clazz : StateHandler.class.getPermittedSubclasses()) {
            if (!clazz.isAnnotationPresent(HandledState.class)) {
                continue;
            }

            HandledState annotation = clazz.getAnnotation(HandledState.class);
            CallState state = annotation.value();

            if (map.containsKey(state)) {
                throw new IllegalStateException("Duplicate handler for state " + state + ": " + clazz.getName());
            }

            try {

                StateHandler handler;
                try{
                    handler = (StateHandler) clazz.getField("INSTANCE").get(null);
                }
                catch (NoSuchFieldException e){
                    handler = (StateHandler) clazz.getDeclaredConstructor().newInstance();
                }

                map.put(state, handler);
                logger.info("Registered handler {} for state {}", clazz.getSimpleName(), state);
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate handler for " + state + ": " + clazz.getName(), e);
            }
        }

        return Collections.unmodifiableMap(map);
    }
}
