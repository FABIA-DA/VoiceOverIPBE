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
import at.htlleonding.fabia.client.formbe.OptionResponseService;
import at.htlleonding.fabia.client.formbe.dtos.Option;
import at.htlleonding.fabia.client.formbe.dtos.SingleChoiceField;
import at.htlleonding.fabia.client.formbe.dtos.requests.OptionResponseCreationRequest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
@HandledState(CallState.SINGLE_CHOICE_FIELD)
public final class SingleChoiceFieldHandler extends StateHandler {
    @RestClient
    SpeechGenerationService speechGenerationService;
    @RestClient
    OptionResponseService optionResponseService;
    @Inject
    AriUtil ariUtil;
    @Inject
    ActiveAudioRegistry activeAudioRegistry;

    @Override
    protected void handleSingleItem(CallSession session) {
        if (!session.fieldGroupValid()) {
            throw new IllegalStateException("Did not select a form or field group");
        }

        final String singleChoiceFieldSpeech = "single-choice-field-speech";
        final String optionSpeech = "option-speech";

        if(session.singleChoiceFieldsEmpty()){
            session.goToField();
            return;
        }

        if(!session.getSingleChoiceFieldHandlingState().isRetry()){
            session.tryIncreaseSingleChoiceFieldIdx();
        }

        if (!session.currentSingleChoiceFieldInBounds()) {
            throw new IllegalStateException("Single Choice Field Idx not in bounds");
        }

        SingleChoiceField field = session.getCurrentSingleChoiceField();
        String options = Util.ConcatItems(field.getOptions(), Option::getName);

        speechGenerationService.generateSpeech(new CoquiRequest(field.getName(), singleChoiceFieldSpeech)).await().indefinitely();
        speechGenerationService.generateSpeech(new CoquiRequest(options, optionSpeech)).await().indefinitely();

        session.enqueueAudio(new PlaybackItem("for-the-field", session.getChannelId(), ariUtil, activeAudioRegistry));
        session.enqueueAudio(new PlaybackItem(singleChoiceFieldSpeech, session.getChannelId(), ariUtil, activeAudioRegistry));
        session.enqueueAudio(new PlaybackItem("options-are", session.getChannelId(), ariUtil, activeAudioRegistry));
        session.enqueueAudio(new PlaybackItem(optionSpeech, session.getChannelId(), ariUtil, activeAudioRegistry));
    }

    @Override
    protected void handleRequestInput(CallSession session) {
        session.enqueueAudio(new PlaybackItem("single-choice-field-input-request", session.getChannelId(), ariUtil, activeAudioRegistry));
        RecordingItem recording = new RecordingItem(session.getChannelId(), ariUtil, activeAudioRegistry);
        session.getSingleChoiceFieldHandlingState().setRecording(recording);
        session.enqueueAudio(recording);
    }

    @Override
    protected void handleProcessInput(CallSession session) {
        RecordingItem recording = session.getSingleChoiceFieldHandlingState().getRecording();
        if (recording == null) {
            throw new IllegalStateException("No recording happened before input processing");
        }

        String text = transcribe(recording).await().indefinitely();

        if (text == null) {
            return;
        }

        for (Option option : session.getCurrentSingleChoiceField().getOptions()) {
            Pattern pattern = Pattern.compile(option.getName(), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                session.getSingleChoiceFieldHandlingState().setOptionMatch(true);
                optionResponseService.createOptionResponse(new OptionResponseCreationRequest(option.getId(), session.getChannelName()))
                        .await()
                        .indefinitely();
                session.addFields(option.getFields());
                break;
            }
        }
    }

    @Override
    protected void handleRetry(CallSession session) {
        if (session.getSingleChoiceFieldHandlingState().isOptionMatch()) {
            session.getSingleChoiceFieldHandlingState().setRetry(false);
            return;
        }

        session.enqueueAudio(new PlaybackItem("option-not-found", session.getChannelId(), ariUtil, activeAudioRegistry));
        session.getSingleChoiceFieldHandlingState().setRetry(true);
        session.resetBaseState();
    }

    @Override
    protected void handleDone(CallSession session) {
        if (session.singleChoiceFieldsLeft()) {
            session.resetBaseState();
        } else {
            super.handleDone(session);
        }
    }
}
