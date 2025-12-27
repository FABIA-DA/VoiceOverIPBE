package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.util.BaseState;
import at.htlleonding.fabia.callmgmt.CallSession;
import at.htlleonding.fabia.callmgmt.audiomgmt.RecordingItem;
import at.htlleonding.fabia.client.whisperbe.TranscriptionService;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;

/**
 * Handler for a specific call state to handle all needed base states.
 */
public sealed abstract class StateHandler permits FieldGroupHandler, FieldHandler, FormHandler, GoodbyeHandler, GreetingHandler, GroupHandler, SingleChoiceFieldHandler {
    @RestClient
    TranscriptionService transcriptionService;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Tries to handle the current step.
     * @param session The call session to process
     */
    public void handle(CallSession session) {

        try {
            switch (session.getCurrentBaseState()) {
                case BaseState.INFO -> handleInfo(session);
                case BaseState.LIST -> handleList(session);
                case BaseState.SINGLE_ITEM -> handleSingleItem(session);
                case BaseState.REQUEST_INPUT -> handleRequestInput(session);
                case BaseState.PROCESS_INPUT -> handleProcessInput(session);
                case BaseState.RETRY -> handleRetry(session);
                case BaseState.DONE -> handleDone(session);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        session.nextAudioOrStep();
    }

    /**
     * Handles the Info base state.
     * @param session The session to handle
     */
    protected void handleInfo(CallSession session) throws IOException {
    }

    /**
     * Handles the List base state.
     * @param session The session to handle for it
     */
    protected void handleList(CallSession session) {
    }

    /**
     * Handles the Single Item base state.
     * @param session The session to handle
     */
    protected void handleSingleItem(CallSession session) {
    }

    /**
     * Handles the Request Input base state.
     * @param session The session to handle
     */
    protected void handleRequestInput(CallSession session) {
    }

    /**
     * Handles the Process Input base state.
     * @param session The session to handle
     */
    protected void handleProcessInput(CallSession session) {
    }

    /**
     * Handles the Retry base state.
     * @param session The session to handle
     */
    protected void handleRetry(CallSession session) {
    }

    /**
     * Handles the Done base state.
     * @param session The session to handle
     */
    protected void handleDone(CallSession session) {
        session.advanceCallState();
    }

    /**
     * Transcribes a recording.
     * @param recording The recording item with the file name
     * @return The transcribed text
     */
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
