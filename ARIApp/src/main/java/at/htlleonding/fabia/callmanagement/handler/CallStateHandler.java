package at.htlleonding.fabia.callmanagement.handler;

import at.htlleonding.fabia.callmanagement.CallSession;

public sealed interface CallStateHandler permits EnterFormHandler, EnterGroupHandler, FieldGroupHandler, FormIntroHandler, GreetingHandler {
    void handle(CallSession session);
}
