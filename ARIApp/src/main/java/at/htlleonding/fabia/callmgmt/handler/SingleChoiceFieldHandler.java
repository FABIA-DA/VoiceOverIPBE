package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.*;
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

    @Override
    protected Uni<Void> handleSingleItemAsync(CallSession session) {
        if (!session.fieldGroupValid()) {
            logger.error("Did not select a form or field group");
            session.enqueue("field-group-invalid", playbackLookup.error);
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
            session.enqueue("scf-out-of-bounds", playbackLookup.error);
            session.goToGoodbye();
            return Uni.createFrom().voidItem();
        }

        final String singleChoiceFieldSpeech = "single-choice-field-speech";
        final String optionSpeech = "option-speech";
        SingleChoiceField field = session.getCurrentSingleChoiceField();
        String options = Util.ConcatItems(field.getOptions(), Option::getName);

        return ariUtil.startMohAsync(session.getChannelId())
                .chain(() ->
                        Uni.combine()
                                .all()
                                .unis(speechGenerationService.generateSpeech(
                                                new CoquiRequest(field.getName(), singleChoiceFieldSpeech)),
                                        speechGenerationService.generateSpeech(
                                                new CoquiRequest(options, optionSpeech)))
                                .asTuple())
                .invoke(() -> {
                    session.enqueue("scf-intro",
                            playbackLookup.fieldIntro,
                            singleChoiceFieldSpeech,
                            playbackLookup.optionsAre,
                            optionSpeech);
                })
                .replaceWithVoid()
                .eventually(() -> ariUtil.endMohAsync(session.getChannelId()));
    }

    @Override
    protected Uni<Void> handleRequestInputAsync(CallSession session) {
        return Uni.createFrom().voidItem()
                .invoke(() -> {
                    session.enqueue("scf-input", playbackLookup.singleChoiceFieldInputRequest);
                    session.getSingleChoiceFieldHandlingState().setRecording(session.enqueue());
                });
    }

    @Override
    protected Uni<Void> handleProcessInputAsync(CallSession session) {
        RecordingItem recording = session.getSingleChoiceFieldHandlingState().getRecording();
        if (recording == null) {
            logger.error("No recording happened before input processing");
            session.enqueue("scf-recording-null", playbackLookup.error);
            session.goToGoodbye();
            return Uni.createFrom().voidItem();
        }

        return ariUtil.startMohAsync(session.getChannelId())
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

                    if (optionId == -1) {
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
                .eventually(() -> ariUtil.endMohAsync(session.getChannelId()));

        if (session.getSingleChoiceFieldHandlingState().isOptionMatch()) {
            session.getSingleChoiceFieldHandlingState().setRetry(false);
            return endMoh;
        }

        session.getSingleChoiceFieldHandlingState().setRetry(true);
        session.resetBaseState();

        String transcript = session.getSingleChoiceFieldHandlingState().getTranscript();
        if (transcript == null || transcript.isBlank()) {
            session.enqueue("scf-could-not-understand", playbackLookup.couldNotUnderstand);
            return endMoh;
        }

        final String userTranscriptSpeech = "scf-user-transcript";

        return ariUtil.startMohAsync(session.getChannelId())
                .chain(() -> speechGenerationService.generateSpeech(
                        new CoquiRequest(
                                session.getSingleChoiceFieldHandlingState().getTranscript(),
                                userTranscriptSpeech)))
                .invoke(() -> {
                    session.enqueue("scf-retry",
                            playbackLookup.couldNotUnderstand,
                            playbackLookup.iHeard,
                            userTranscriptSpeech);
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
