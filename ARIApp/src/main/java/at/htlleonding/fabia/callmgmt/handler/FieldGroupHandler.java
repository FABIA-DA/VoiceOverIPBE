package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.util.CallState;
import at.htlleonding.fabia.callmgmt.util.HandledState;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;

@Singleton
@HandledState(CallState.FIELD_GROUP)
public final class FieldGroupHandler extends StateHandler {
    @Override
    protected Uni<Void> handleInfoAsync(CallSession session) {
        if (session.getSelectedForm() == null) {
            logger.error("No Form was selected");
            session.enqueue("form-null", "error");
            session.goToGoodbye();
            return Uni.createFrom().voidItem();
        }

        if (session.fieldGroupsEmpty()) {
            session.enqueue("form-empty", "form-empty");
            session.goToGoodbye();
            return Uni.createFrom().voidItem();
        }

        session.tryIncreaseFieldGroupIdx();

        return Uni.createFrom().voidItem();
    }

    @Override
    protected Uni<Void> handleSingleItemAsync(CallSession session) {
        if (session.currentFieldGroupInBounds()) {
            session.getUsedFields().clear();
        }

        return Uni.createFrom().voidItem();
    }
}
