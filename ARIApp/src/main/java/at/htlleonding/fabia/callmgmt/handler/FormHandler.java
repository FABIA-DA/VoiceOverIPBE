package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.audiomgmt.RecordingItem;
import at.htlleonding.fabia.callmgmt.util.*;
import at.htlleonding.fabia.client.coquibe.CoquiRequest;
import at.htlleonding.fabia.client.coquibe.SpeechGenerationService;
import at.htlleonding.fabia.client.formbe.dtos.Form;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@Singleton
@HandledState(CallState.FORM)
public final class FormHandler extends StateHandler {
    @RestClient
    SpeechGenerationService speechGenerationService;
    @Inject
    AriUtil ariUtil;

    @Override
    protected Uni<Void> handleListAsync(CallSession session) {
        final String formsSpeechName = "form-names";

        return ariUtil.startMohAsync(session.getChannelId())
                .flatMap(v -> {
                    if (session.getSelectedGroup() == null) {
                        logger.error("No group for form intro was selected");
                        session.enqueue("group-null", playbackLookup.error);
                        session.goToGoodbye();
                        return Uni.createFrom().voidItem();
                    }

                    List<Form> forms = session.getSelectedGroup().getForms();

                    if (forms.isEmpty()) {
                        session.enqueue("group-empty", playbackLookup.groupEmpty);
                        session.goToGoodbye();
                        return Uni.createFrom().voidItem();
                    }

                    String names = Util.ConcatItems(forms, Form::getName);

                    return speechGenerationService.generateSpeech(
                            new CoquiRequest(names, formsSpeechName));
                })
                .invoke(() -> {
                    if (session.getState() == CallState.GOODBYE) {
                        return;
                    }

                    session.enqueue("form-preamble",
                            playbackLookup.formPreamble,
                            formsSpeechName);
                })
                .eventually(() -> ariUtil.endMohAsync(session.getChannelId()));
    }

    @Override
    protected Uni<Void> handleRequestInputAsync(CallSession session) {
        return Uni.createFrom().voidItem().invoke(() -> {
            session.enqueue("form-input-request", playbackLookup.formInputRequest);
            session.getFormHandlingState().setRecording(session.enqueue());
        });
    }

    @Override
    protected Uni<Void> handleProcessInputAsync(CallSession session) {
        RecordingItem recording = session.getFormHandlingState().getRecording();
        if (recording == null) {
            logger.error("No recording happened before input processing");
            session.enqueue("form-recording-null", playbackLookup.error);
            session.goToGoodbye();
            return Uni.createFrom().voidItem();
        }

        return ariUtil.startMohAsync(session.getChannelId())
                .chain(() -> transcribe(recording))
                .invoke(text -> {
                    session.getFormHandlingState().setTranscript(text);

                    if (text == null || text.isBlank()) {
                        return;
                    }

                    for (Form form : session.getSelectedGroup().getForms()) {
                        if (text.replace(" ", "")
                                .toLowerCase()
                                .contains(form.getName().toLowerCase())) {
                            session.setSelectedForm(form);
                            break;
                        }
                    }
                })
                .replaceWithVoid();
    }

    @Override
    protected Uni<Void> handleRetryAsync(CallSession session) {
        Uni<Void> endMoh = Uni.createFrom()
                .voidItem()
                .eventually(() -> ariUtil.endMohAsync(session.getChannelId()));
        if (session.getSelectedForm() != null) {
            return endMoh;
        }

        session.resetBaseState();

        String transcript = session.getFormHandlingState().getTranscript();
        if (transcript == null || transcript.isBlank()) {
            session.enqueue("form-input-empty", playbackLookup.couldNotUnderstand);
            return endMoh;
        }

        final String userTranscriptSpeech = "form-user-transcript";

        return
                speechGenerationService.generateSpeech(
                                new CoquiRequest(
                                        session.getFormHandlingState().getTranscript(),
                                        userTranscriptSpeech))
                        .invoke(() -> {
                            session.enqueue("form-check",
                                    playbackLookup.iHeard,
                                    userTranscriptSpeech);
                        })
                        .eventually(() -> endMoh);
    }
}
