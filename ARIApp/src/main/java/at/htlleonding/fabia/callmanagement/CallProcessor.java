package at.htlleonding.fabia.callmanagement;

import at.htlleonding.fabia.callmanagement.handler.CallStateHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public final class CallProcessor {
    private static final HashMap<CallState, CallStateHandler> handlerMap = new HashMap<>();

    public static void process(CallSession session) {
        if (handlerMap.isEmpty()) {
            try {
                initMap();
            } catch (RuntimeException e) {
                System.out.println("Error handling state " + session.getState());
                e.printStackTrace();
                CallAudio.enqueueError(session);
            }
        }

        CallStateHandler handler = handlerMap.get(session.getState());
        if (handler == null) {
            System.out.println("No handler for state " + session.getState());
            CallAudio.enqueueError(session);
            return;
        }

        handler.handle(session);
    }

    private static void initMap() {
        Class<?>[] handlers = CallStateHandler.class.getPermittedSubclasses();

        for (Class<?> handler : handlers) {
            HandledState state = handler.getAnnotation(HandledState.class);
            if (handler.isAnnotationPresent(HandledState.class)) {
                HandledState handledState = handler.getAnnotation(HandledState.class);

                @SuppressWarnings("unchecked")
                Class<? extends CallStateHandler> handlerClass = (Class<? extends CallStateHandler>) handler;
                try {
                    handlerMap.put(handledState.value(), handlerClass.getDeclaredConstructor().newInstance());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
