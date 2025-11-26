package at.htlleonding.fabia.callmgmt;

import at.htlleonding.fabia.callmgmt.handler.StateHandler;
import at.htlleonding.fabia.callmgmt.util.CallState;
import at.htlleonding.fabia.callmgmt.util.HandledState;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Singleton
public final class CallProcessor {
    private final Logger logger = LoggerFactory.getLogger(CallProcessor.class);
    private final Map<CallState, StateHandler> handlerMap;

    @Inject
    public CallProcessor(Instance<StateHandler> handlers) {
        Map<CallState, StateHandler> map = new EnumMap<>(CallState.class);

        for (StateHandler handler : handlers) {
            HandledState annotation = handler.getClass().getAnnotation(HandledState.class);
            if (annotation == null) {
                continue;
            }

            CallState state = annotation.value();
            if (map.containsKey(state)) {
                throw new IllegalStateException(
                        "Duplicate handler for state " + state + ": " + handler.getClass().getName());
            }

            map.put(state, handler);
            logger.info("Registered handler {} for state {}", handler.getClass().getSimpleName(), state);
        }

        this.handlerMap = Collections.unmodifiableMap(map);
    }

    public void process(CallSession session) {
        logger.debug("Processing session {} in state {}", session.getChannelId(), session.getState());

        StateHandler handler = handlerMap.get(session.getState());

        if (handler == null) {
            logger.warn("No handler registered for state {}", session.getState());
            return;
        }

        try {
            handler.handle(session);
        } catch (Exception e) {
            logger.error("Error handling state {} for session {} - Handler: {}",
                    session.getState(),
                    session.getChannelId(),
                    handler.getClass().getSimpleName(),
                    e);
        }
    }
}
