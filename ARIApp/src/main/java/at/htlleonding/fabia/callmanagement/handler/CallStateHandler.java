package at.htlleonding.fabia.callmanagement.handler;

import at.htlleonding.fabia.callmanagement.CallSession;

public sealed interface CallStateHandler permits GroupHandler, FieldGroupHandler, FormHandler, GreetingHandler {
    void handle(CallSession session);
}
