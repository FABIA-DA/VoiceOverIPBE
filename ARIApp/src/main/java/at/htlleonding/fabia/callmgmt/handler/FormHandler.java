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
    protected void handleList(CallSession session) {
        final String formsSpeechName = "form-names";

        if (session.getSelectedGroup() == null) {
            throw new IllegalStateException("No group for form intro was selected");
        }

        List<Form> forms = session.getSelectedGroup().getForms();

        if(forms.isEmpty()){
            session.enqueueAudio(new PlaybackItem("group-empty", session.getChannelId(), ariUtil, activeAudioRegistry));
            session.goToGoodbye();
            return;
        }

        String names = Util.ConcatItems(forms, Form::getName);

        speechGenerationService.generateSpeech(new CoquiRequest("Wir haben diese Formulare verfügbar: " + names, formsSpeechName)).await().indefinitely();
        session.enqueueAudio(new PlaybackItem(formsSpeechName, session.getChannelId(), ariUtil, activeAudioRegistry));
    }

    @Override
    protected void handleRequestInput(CallSession session) {
        session.enqueueAudio(new PlaybackItem("form-input-request", session.getChannelId(), ariUtil, activeAudioRegistry));
        RecordingItem recordingItem = new RecordingItem(session.getChannelId(), ariUtil, activeAudioRegistry);
        session.getFormHandlingState().setRecording(recordingItem);
        session.enqueueAudio(recordingItem);
    }

    @Override
    protected void handleProcessInput(CallSession session) {
        RecordingItem recording = session.getFormHandlingState().getRecording();
        if (recording == null) {
            throw new IllegalStateException("No recording happened before input processing");
        }

        String text = transcribe(recording).await().indefinitely();

        if (text == null) {
            return;
        }

        for (Form form : session.getSelectedGroup().getForms()) {
            Pattern pattern = Pattern.compile(form.getName(), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                session.setSelectedForm(form);
                break;
            }
        }
    }

    @Override
    protected void handleRetry(CallSession session) {
        if (session.getSelectedForm() != null) {
            return;
        }

        session.enqueueAudio(new PlaybackItem("form-not-found", session.getChannelId(), ariUtil, activeAudioRegistry));
        session.resetBaseState();
    }
}
