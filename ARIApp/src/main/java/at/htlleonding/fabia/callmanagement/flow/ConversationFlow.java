package at.htlleonding.fabia.callmanagement.flow;

import at.htlleonding.fabia.callmanagement.CallSession;
import at.htlleonding.fabia.callmanagement.CallState;

import java.util.Optional;

public interface ConversationFlow {
    CallState getCurrentState();
    Optional<CallState> next(CallSession callSession);
    boolean isDone();
}
