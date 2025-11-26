package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.util.BaseState;
import at.htlleonding.fabia.callmgmt.CallSession;
import at.htlleonding.fabia.callmgmt.audiomgmt.RecordingItem;
import at.htlleonding.fabia.client.whisperbe.TranscriptionClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;

public sealed abstract class StateHandler permits FieldGroupHandler, FieldHandler, FormHandler, GoodbyeHandler, GreetingHandler, GroupHandler, SingleChoiceFieldHandler {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void handle(CallSession session) {

        try {
            switch (session.getCurrentBaseState()) {
                case BaseState.INFO -> handleInfo(session);
                case BaseState.LIST -> handleList(session);
                case BaseState.SINGLE_ITEM -> handleSingleItem(session);
                case BaseState.REQUEST_INPUT -> handleRequestInput(session);
                case BaseState.PROCESS_INPUT -> handleProcessInput(session);
                case BaseState.RETRY -> handleRetry(session);
                case BaseState.CONFIRM -> handleConfirm(session);
                case BaseState.DECIDE -> handleDecide(session);
                case BaseState.DONE -> handleDone(session);
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

    protected void handleSingleItem(CallSession session) throws IOException {
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

    protected void handleDone(CallSession session) {
        session.advanceCallState();
    }

    protected String transcribe(RecordingItem recording) throws IOException {
        String filePath = MessageFormat.format("/app/recordings/{0}.wav", recording.getName());

        String text = TranscriptionClient.getClient().transcribe(filePath);

        logger.debug("Transcribed text: {}", text);

        if (text == null) {
            return "";
        }

        return text;
    }
}
