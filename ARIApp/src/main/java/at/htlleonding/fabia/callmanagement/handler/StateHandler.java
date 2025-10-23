package at.htlleonding.fabia.callmanagement.handler;

import at.htlleonding.fabia.callmanagement.BaseState;
import at.htlleonding.fabia.callmanagement.CallSession;
import at.htlleonding.fabia.callmanagement.CallState;

import java.io.IOException;

public sealed abstract class StateHandler permits GreetingHandler, GroupHandler, FormHandler, FieldGroupHandler {
    public void handle(CallSession session) {

        try {
            switch (session.getCurrentBaseState()) {
                case BaseState.Info -> handleInfo(session);
                case BaseState.List -> handleList(session);
                case BaseState.RequestInput -> handleRequestInput(session);
                case BaseState.ProcessInput -> handleProcessInput(session);
                case BaseState.Confirm -> handleConfirm(session);
                case BaseState.Decide -> handleDecide(session);
                case BaseState.Done -> session.setState(nextState());

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        session.nextAudioOrStep();
    }

    protected abstract CallState nextState();

    protected void handleInfo(CallSession session) {
    }

    protected void handleList(CallSession session) throws IOException {
    }

    protected void handleRequestInput(CallSession session) {
    }

    protected void handleProcessInput(CallSession session) throws IOException {
    }

    protected void handleConfirm(CallSession session) {
    }

    protected void handleDecide(CallSession session) {
    }
}
