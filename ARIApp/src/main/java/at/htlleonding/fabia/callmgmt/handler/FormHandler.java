package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.audiomgmt.ActiveAudioRegistry;
import at.htlleonding.fabia.callmgmt.audiomgmt.PlaybackItem;
import at.htlleonding.fabia.callmgmt.audiomgmt.RecordingItem;
import at.htlleonding.fabia.callmgmt.util.AriUtil;
import at.htlleonding.fabia.callmgmt.util.CallState;
import at.htlleonding.fabia.callmgmt.util.Util;
import at.htlleonding.fabia.callmgmt.util.HandledState;
import at.htlleonding.fabia.client.coquibe.CoquiRequest;
import at.htlleonding.fabia.client.coquibe.SpeechGenerationService;
import at.htlleonding.fabia.client.formbe.dtos.Form;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
@HandledState(CallState.FORM)
public final class FormHandler extends StateHandler {
    @RestClient
    SpeechGenerationService speechGenerationService;
    @Inject
    AriUtil ariUtil;
    @Inject
    ActiveAudioRegistry activeAudioRegistry;

    @Override
    protected Uni<Void> handleListAsync(CallSession session) {
        final String formsSpeechName = "form-names";

        return ariUtil.startMohAsync(session.getBridgeId())
                .flatMap(success -> {
                    if (session.getSelectedGroup() == null) {
                        throw new IllegalStateException("No group for form intro was selected");
                    }

                    List<Form> forms = session.getSelectedGroup().getForms();

                    if (forms.isEmpty()) {
                        session.enqueueAudio(
                                new PlaybackItem("group-empty",
                                        session.getBridgeId(),
                                        ariUtil,
                                        activeAudioRegistry));
                        session.goToGoodbye();
                        return Uni.createFrom().voidItem();
                    }

                    String names = Util.ConcatItems(forms, Form::getName);

                    session.enqueueAudio(
                            new PlaybackItem("form-preamble",
                                    session.getBridgeId(),
                                    ariUtil,
                                    activeAudioRegistry));

                    return speechGenerationService.generateSpeech(
                            new CoquiRequest(names, formsSpeechName));
                })
                .invoke(() -> {
                    session.enqueueAudio(
                            new PlaybackItem(formsSpeechName,
                                    session.getBridgeId(),
                                    ariUtil,
                                    activeAudioRegistry));
                })
                .eventually(() -> ariUtil.endMohAsync(session.getBridgeId()));
    }

    @Override
    protected Uni<Void> handleRequestInputAsync(CallSession session) {
        return Uni.createFrom().voidItem().invoke(() -> {
            session.enqueueAudio(
                    new PlaybackItem("form-input-request",
                            session.getBridgeId(),
                            ariUtil,
                            activeAudioRegistry));
            RecordingItem recordingItem = new RecordingItem(session.getBridgeId(), ariUtil, activeAudioRegistry);
            session.getFormHandlingState().setRecording(recordingItem);
            session.enqueueAudio(recordingItem);
        });
    }

    @Override
    protected Uni<Void> handleProcessInputAsync(CallSession session) {
        RecordingItem recording = session.getFormHandlingState().getRecording();
        if (recording == null) {
            throw new IllegalStateException("No recording happened before input processing");
        }

        return ariUtil.startMohAsync(session.getBridgeId())
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
                .eventually(() -> ariUtil.endMohAsync(session.getBridgeId()));
        if (session.getSelectedForm() != null) {
            return endMoh;
        }

        final String userTranscriptSpeech = "form-user-transcript";

        return
                speechGenerationService.generateSpeech(
                                new CoquiRequest(
                                        session.getFormHandlingState().getTranscript(),
                                        userTranscriptSpeech))
                        .invoke(() -> {
                            session.enqueueAudio(
                                    new PlaybackItem("we-understood",
                                            session.getBridgeId(),
                                            ariUtil,
                                            activeAudioRegistry));
                            session.enqueueAudio(
                                    new PlaybackItem(userTranscriptSpeech,
                                            session.getBridgeId(),
                                            ariUtil,
                                            activeAudioRegistry));
                            session.resetBaseState();
                        })
                        .eventually(() -> endMoh);
    }
}
