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
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.rest.client.inject.RestClient;

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
    protected Uni<Void> handleSingleItemAsync(CallSession session) {
        if (!session.fieldGroupValid()) {
            logger.error("Did not select a form or field group");
            session.enqueueAudio(new PlaybackItem("error", session.getBridgeId(), ariUtil, activeAudioRegistry));
            session.goToGoodbye();
            return Uni.createFrom().voidItem();
        }

        if (session.singleChoiceFieldsEmpty()) {
            logger.debug("Single Choice Fields empty transitioning to Fields...");
            session.goToField();
            return Uni.createFrom().voidItem();
        }

        if (!session.getSingleChoiceFieldHandlingState().isRetry()) {
            logger.debug("Trying to increase the Single Choice Field Index");
            session.tryIncreaseSingleChoiceFieldIdx();
        }

        if (!session.currentSingleChoiceFieldInBounds()) {
            logger.error("Single Choice Field Idx not in bounds");
            session.enqueueAudio(new PlaybackItem("error", session.getBridgeId(), ariUtil, activeAudioRegistry));
            session.goToGoodbye();
            return Uni.createFrom().voidItem();
        }

        final String singleChoiceFieldSpeech = "single-choice-field-speech";
        final String optionSpeech = "option-speech";
        SingleChoiceField field = session.getCurrentSingleChoiceField();
        String options = Util.ConcatItems(field.getOptions(), Option::getName);

        return ariUtil.startMohAsync(session.getBridgeId())
                .chain(() ->
                        Uni.combine()
                                .all()
                                .unis(speechGenerationService.generateSpeech(
                                                new CoquiRequest(field.getName(), singleChoiceFieldSpeech)),
                                        speechGenerationService.generateSpeech(
                                                new CoquiRequest(options, optionSpeech)))
                                .asTuple())
                .invoke(() -> {
                    session.enqueueAudio(
                            new PlaybackItem("field-intro",
                                    session.getBridgeId(),
                                    ariUtil,
                                    activeAudioRegistry));
                    session.enqueueAudio(
                            new PlaybackItem(singleChoiceFieldSpeech,
                                    session.getBridgeId(),
                                    ariUtil,
                                    activeAudioRegistry));
                    session.enqueueAudio(
                            new PlaybackItem("options-are",
                                    session.getBridgeId(),
                                    ariUtil,
                                    activeAudioRegistry));
                    session.enqueueAudio(
                            new PlaybackItem(optionSpeech,
                                    session.getBridgeId(),
                                    ariUtil,
                                    activeAudioRegistry));
                })
                .replaceWithVoid()
                .eventually(() -> ariUtil.endMohAsync(session.getBridgeId()));
    }

    @Override
    protected Uni<Void> handleRequestInputAsync(CallSession session) {
        return Uni.createFrom().voidItem()
                .invoke(() -> {
                    session.enqueueAudio(new PlaybackItem("single-choice-field-input-request", session.getBridgeId(), ariUtil, activeAudioRegistry));
                    RecordingItem recording = new RecordingItem(session.getBridgeId(), ariUtil, activeAudioRegistry);
                    session.getSingleChoiceFieldHandlingState().setRecording(recording);
                    session.enqueueAudio(recording);
                });
    }

    @Override
    protected Uni<Void> handleProcessInputAsync(CallSession session) {
        RecordingItem recording = session.getSingleChoiceFieldHandlingState().getRecording();
        if (recording == null) {
            logger.error("No recording happened before input processing");
            session.enqueueAudio(new PlaybackItem("error", session.getBridgeId(), ariUtil, activeAudioRegistry));
            session.goToGoodbye();
            return Uni.createFrom().voidItem();
        }

        return ariUtil.startMohAsync(session.getBridgeId())
                .chain(() -> transcribe(recording))
                .flatMap(text -> {
                    session.getSingleChoiceFieldHandlingState().setTranscript(text);

                    if (text == null || text.isBlank()) {
                        return Uni.createFrom().voidItem();
                    }

                    long optionId = -1;

                    for (Option option : session.getCurrentSingleChoiceField().getOptions()) {
                        if (text.toLowerCase().contains(option.getName().toLowerCase())) {
                            session.getSingleChoiceFieldHandlingState().setOptionMatch(true);
                            optionId = option.getId();

                            session.addFields(option.getFields());
                            break;
                        }
                    }

                    if(optionId == -1){
                        return Uni.createFrom().voidItem();
                    }

                    return optionResponseService.createOptionResponse(
                            new OptionResponseCreationRequest(
                                    optionId,
                                    session.getChannelName()));
                })
                .replaceWithVoid();
    }

    @Override
    protected Uni<Void> handleRetryAsync(CallSession session) {
        Uni<Void> endMoh = Uni.createFrom().voidItem()
                .eventually(() -> ariUtil.endMohAsync(session.getBridgeId()));

        if (session.getSingleChoiceFieldHandlingState().isOptionMatch()) {
            session.getSingleChoiceFieldHandlingState().setRetry(false);
            return endMoh;
        }

        session.getSingleChoiceFieldHandlingState().setRetry(true);
        session.resetBaseState();

        String transcript = session.getSingleChoiceFieldHandlingState().getTranscript();
        if(transcript == null || transcript.isBlank()){
            session.enqueueAudio(
                    new PlaybackItem("could-not-understand",
                            session.getBridgeId(),
                            ariUtil,
                            activeAudioRegistry));
            return endMoh;
        }

        final String userTranscriptSpeech = "scf-user-transcript";

        return ariUtil.startMohAsync(session.getBridgeId())
                .chain(() -> speechGenerationService.generateSpeech(
                        new CoquiRequest(
                                session.getSingleChoiceFieldHandlingState().getTranscript(),
                                userTranscriptSpeech)))
                .invoke(() -> {
                    session.enqueueAudio(
                            new PlaybackItem("could-not-understand",
                                    session.getBridgeId(),
                                    ariUtil,
                                    activeAudioRegistry));
                    session.enqueueAudio(
                            new PlaybackItem("i-heard",
                                    session.getBridgeId(),
                                    ariUtil,
                                    activeAudioRegistry));
                    session.enqueueAudio(
                            new PlaybackItem(userTranscriptSpeech,
                                    session.getBridgeId(),
                                    ariUtil,
                                    activeAudioRegistry));
                })
                .eventually(() -> endMoh);
    }

    @Override
    protected Uni<Void> handleDoneAsync(CallSession session) {
        if (session.singleChoiceFieldsLeft()) {
            return Uni.createFrom().voidItem().invoke(session::resetBaseState);
        } else {
            return super.handleDoneAsync(session);
        }
    }
}
