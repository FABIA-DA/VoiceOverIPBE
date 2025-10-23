package at.htlleonding.fabia.callmanagement;

import at.htlleonding.fabia.callmanagement.handler.StateHandler;

import java.util.HashMap;

public final class CallProcessor {
    private static final HashMap<CallState, StateHandler> handlerMap = new HashMap<>();

    public static void process(CallSession session) {
        if (handlerMap.isEmpty()) {
            try {
                initMap();
            } catch (RuntimeException e) {
                System.out.println("Error handling state " + session.getState());
                e.printStackTrace();
            }
        }

        StateHandler handler = handlerMap.get(session.getState());
        if (handler == null) {
            System.out.println("No handler for state " + session.getState());
            return;
        }

        handler.handle(session);
    }

    private static void initMap() {
        Class<?>[] handlers = StateHandler.class.getPermittedSubclasses();

        for (Class<?> handler : handlers) {
            HandledState state = handler.getAnnotation(HandledState.class);
            if (handler.isAnnotationPresent(HandledState.class)) {
                HandledState handledState = handler.getAnnotation(HandledState.class);

                @SuppressWarnings("unchecked")
                Class<? extends StateHandler> handlerClass = (Class<? extends StateHandler>) handler;
                try {
                    handlerMap.put(handledState.value(), handlerClass.getDeclaredConstructor().newInstance());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
