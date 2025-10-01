package at.htlleonding.fabia.callmanagement;

import io.netty.util.Recycler;

import java.lang.reflect.InvocationTargetException;

public final class CallProcessor {
    public static void process(CallSession session) {
        Class<?>[] handlers = CallStateHandler.class.getPermittedSubclasses();

        for(Class<?> handler : handlers) {
            HandledState state = handler.getAnnotation(HandledState.class);
            if(handler.isAnnotationPresent(HandledState.class)
            && state.value() == session.getState()) {
                try {
                    handler.getMethod(CallStateHandler.class.getMethods()[0].getName()).invoke(session);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
