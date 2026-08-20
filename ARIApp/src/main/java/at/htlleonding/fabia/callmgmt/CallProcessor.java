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

    /**
     * Initializes every handler for call states.
     *
     * @param handlers All in Quarkus registered state handlers
     */
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
                logger.warn("Duplicate handler for state {}: {} skipped", state, handler.getClass().getName());
                continue;
            }

            map.put(state, handler);
            logger.info("Registered handler {} for state {}", handler.getClass().getSimpleName(), state);
        }

        this.handlerMap = Collections.unmodifiableMap(map);
    }

    /**
     * Tries to process the next step for a session.
     *
     * @param session The call session to advance
     */
    public void process(CallSession session) {
        logger.debug("Processing session with channel id {} in state {}", session.getChannelId(), session.getState());

        StateHandler handler = handlerMap.get(session.getState());

        if (handler == null) {
            logger.warn("No handler registered for state {}", session.getState());
            return;
        }


        handler.handleAsync(session)
                .subscribe().with(
                        success -> {
                        },
                        failure -> {
                            logger.error("Error handling state {} for session {} - Handler: {}",
                                    session.getState(),
                                    session.getChannelId(),
                                    handler.getClass().getSimpleName(),
                                    failure);
                        }
                );
    }
}
