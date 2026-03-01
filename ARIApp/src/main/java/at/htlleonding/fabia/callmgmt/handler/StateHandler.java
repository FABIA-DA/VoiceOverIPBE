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

import static at.htlleonding.fabia.callmgmt.util.BaseState.*;

/**
 * Handler for a specific call state to handle all needed base states.
 */
public sealed abstract class StateHandler permits FieldGroupHandler, FieldHandler, FormHandler, GoodbyeHandler, GreetingHandler, GroupHandler, SingleChoiceFieldHandler {
    @RestClient
    TranscriptionService transcriptionService;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Tries to handle the current step.
     *
     * @param session The call session to process
     */
    public Uni<Void> handleAsync(CallSession session) {

        Uni<Void> uni = switch (session.getCurrentBaseState()) {
            case START -> Uni.createFrom().voidItem();
            case INFO -> handleInfoAsync(session);
            case LIST -> handleListAsync(session);
            case SINGLE_ITEM -> handleSingleItemAsync(session);
            case REQUEST_INPUT -> handleRequestInputAsync(session);
            case PROCESS_INPUT -> handleProcessInputAsync(session);
            case RETRY -> handleRetryAsync(session);
            case DONE -> handleDoneAsync(session);
        };

        return uni.invoke(session::nextAudioOrStep);
    }

    /**
     * Handles the Info base state.
     *
     * @param session The session to handle
     */
    protected Uni<Void> handleInfoAsync(CallSession session) {
        return Uni.createFrom().voidItem();
    }

    /**
     * Handles the List base state.
     *
     * @param session The session to handle for it
     */
    protected Uni<Void> handleListAsync(CallSession session) {
        return Uni.createFrom().voidItem();
    }

    /**
     * Handles the Single Item base state.
     *
     * @param session The session to handle
     */
    protected Uni<Void> handleSingleItemAsync(CallSession session) {
        return Uni.createFrom().voidItem();
    }

    /**
     * Handles the Request Input base state.
     *
     * @param session The session to handle
     */
    protected Uni<Void> handleRequestInputAsync(CallSession session) {
        return Uni.createFrom().voidItem();
    }

    /**
     * Handles the Process Input base state.
     *
     * @param session The session to handle
     */
    protected Uni<Void> handleProcessInputAsync(CallSession session) {
        return Uni.createFrom().voidItem();
    }

    /**
     * Handles the Retry base state.
     *
     * @param session The session to handle
     */
    protected Uni<Void> handleRetryAsync(CallSession session) {
        return Uni.createFrom().voidItem();
    }

    /**
     * Handles the Done base state.
     *
     * @param session The session to handle
     */
    protected Uni<Void> handleDoneAsync(CallSession session) {
        return Uni.createFrom().deferred(() -> {
            session.advanceCallState();
            return Uni.createFrom().voidItem();
        });
    }

    /**
     * Transcribes a recording.
     *
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
