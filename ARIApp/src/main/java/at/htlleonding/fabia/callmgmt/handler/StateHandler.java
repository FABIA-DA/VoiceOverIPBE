package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.util.BaseState;
import at.htlleonding.fabia.callmgmt.CallSession;
import at.htlleonding.fabia.callmgmt.audiomgmt.RecordingItem;
import at.htlleonding.fabia.client.whisperbe.TranscriptionService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;

public sealed abstract class StateHandler permits FieldGroupHandler, FieldHandler, FormHandler, GoodbyeHandler, GreetingHandler, GroupHandler, SingleChoiceFieldHandler {
    @RestClient
    TranscriptionService transcriptionService;

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

    protected Uni<String> transcribe(RecordingItem recording) {
        String filePath = MessageFormat.format("/app/recordings/{0}.wav", recording.getName());

        return transcriptionService.transcribe(Path.of(filePath))
                .onFailure()
                .recoverWithNull()
                .onItem()
                .transform(response -> {
                    if (response == null) {
                        return null;
                    }

                    return response.getText();
                }).invoke(text -> {
                    if (text != null) {
                        logger.debug("Transcribed text: {}", text);
                    }
                });
    }
}
