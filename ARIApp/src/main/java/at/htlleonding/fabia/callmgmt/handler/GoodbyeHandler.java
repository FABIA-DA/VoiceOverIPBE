package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.CallSession;
import at.htlleonding.fabia.callmgmt.util.CallState;
import at.htlleonding.fabia.callmgmt.util.HandledState;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;

@Singleton
@HandledState(CallState.GOODBYE)
public final class GoodbyeHandler extends StateHandler {
    @Override
    protected Uni<Void> handleInfoAsync(CallSession session) {
        return Uni.createFrom().voidItem().invoke(() ->
                session.enqueue("goodbye", "fabia-goodbye"));
    }

    @Override
    protected Uni<Void> handleDoneAsync(CallSession session) {
        return Uni.createFrom().voidItem().invoke(session::endCall);
    }
}
