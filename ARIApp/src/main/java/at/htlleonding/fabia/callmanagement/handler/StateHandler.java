package at.htlleonding.fabia.callmanagement.handler;

import at.htlleonding.fabia.callmanagement.BaseState;
import at.htlleonding.fabia.callmanagement.CallSession;

import java.io.IOException;

public sealed abstract class StateHandler permits FieldGroupHandler, FormHandler, GreetingHandler, GroupHandler {
    public void handle(CallSession session) {

        try {
            switch (session.getCurrentBaseState()) {
                case BaseState.INFO -> handleInfo(session);
                case BaseState.LIST -> handleList(session);
                case BaseState.REQUEST_INPUT -> handleRequestInput(session);
                case BaseState.PROCESS_INPUT -> handleProcessInput(session);
                case BaseState.RETRY -> handleRetry(session);
                case BaseState.CONFIRM -> handleConfirm(session);
                case BaseState.DECIDE -> handleDecide(session);
                case BaseState.DONE -> session.advanceCallState();

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        session.nextAudioOrStep();
    }

    protected void handleInfo(CallSession session) throws IOException {
    }

    protected void handleList(CallSession session) throws IOException {
    }

    protected void handleRequestInput(CallSession session) throws IOException {
    }

    protected void handleProcessInput(CallSession session) throws IOException {
    }

    protected void handleRetry(CallSession session) {
    }

    protected void handleConfirm(CallSession session) {
    }

    protected void handleDecide(CallSession session) {
    }
}
