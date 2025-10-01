package at.htlleonding.fabia.callmanagement;

import io.netty.util.Recycler;

import java.lang.reflect.InvocationTargetException;

public final class CallProcessor {
    public static void process(CallSession session) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?>[] handlers = CallStateHandler.class.getPermittedSubclasses();

        for(Class<?> handler : handlers) {
            HandledState state = handler.getAnnotation(HandledState.class);
            if(handler.isAnnotationPresent(HandledState.class)
                    && handler.isSealed()
            && state.value() == session.getState()) {
                handler.getMethod(CallStateHandler.class.getMethods()[0].getName()).invoke(session);
            }
        }
    }
}
