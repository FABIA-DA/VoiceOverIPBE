package at.htlleonding.fabia.callmanagement;

public sealed interface CallStateHandler permits EnteringGroupHandler {
    void handle(CallSession session);
}
