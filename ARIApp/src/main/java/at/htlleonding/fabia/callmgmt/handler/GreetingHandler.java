package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.util.CallState;
import at.htlleonding.fabia.callmgmt.util.HandledState;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;

@Singleton
@HandledState(CallState.GREETING)
public final class GreetingHandler extends StateHandler {
    @Override
    protected Uni<Void> handleInfoAsync(CallSession session) {
        return Uni.createFrom().deferred(() -> {
            session.enqueue("greeting", "greeting", "instructions-1", "instructions-2");
            return Uni.createFrom().voidItem();
        });
    }
}