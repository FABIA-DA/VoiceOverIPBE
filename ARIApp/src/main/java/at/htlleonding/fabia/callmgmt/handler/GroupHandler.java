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
import at.htlleonding.fabia.client.formbe.GroupService;
import at.htlleonding.fabia.client.formbe.dtos.GroupListDto;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@Singleton
@HandledState(CallState.GROUP)
public final class GroupHandler extends StateHandler {
    @RestClient
    SpeechGenerationService speechGenerationService;
    @RestClient
    GroupService groupService;
    @Inject
    AriUtil ariUtil;
    @Inject
    ActiveAudioRegistry activeAudioRegistry;

    @Override
    protected Uni<Void> handleListAsync(CallSession session) {
        String groupSpeech = "group-speech";

        return ariUtil.startMohAsync(session.getBridgeId())
                .chain(groupService::getAllGroups)
                .flatMap(groupList -> {
                    List<GroupListDto> groups = groupList.getGroups();

                    if (groups.isEmpty()) {
                        session.enqueueAudio(new PlaybackItem("groups-empty", session.getBridgeId(), ariUtil, activeAudioRegistry));
                        session.goToGoodbye();
                        return Uni.createFrom().voidItem();
                    }

                    session.getGroupHandlingState().setGroupList(groups);
                    String names = Util.ConcatItems(groups, GroupListDto::getName);

                    return speechGenerationService.generateSpeech(new CoquiRequest(names, groupSpeech));
                })
                .invoke(() -> {
                    session.enqueueAudio(new PlaybackItem("form-group-preamble", session.getBridgeId(), ariUtil, activeAudioRegistry));
                    session.enqueueAudio(new PlaybackItem(groupSpeech, session.getBridgeId(), ariUtil, activeAudioRegistry));
                })
                .eventually(() -> ariUtil.endMohAsync(session.getBridgeId()));
    }

    @Override
    protected Uni<Void> handleRequestInputAsync(CallSession session) {
        return Uni.createFrom().voidItem()
                .invoke(() -> {
                    session.enqueueAudio(
                            new PlaybackItem("group-input-request",
                                    session.getBridgeId(),
                                    ariUtil,
                                    activeAudioRegistry));
                    RecordingItem recording = new RecordingItem(session.getBridgeId(), ariUtil, activeAudioRegistry);
                    session.getGroupHandlingState().setRecording(recording);
                    session.enqueueAudio(recording);
                });
    }

    @Override
    protected Uni<Void> handleProcessInputAsync(CallSession session) {
        RecordingItem recording = session.getGroupHandlingState().getRecording();
        if (recording == null) {
            throw new IllegalStateException("No recording happened before input processing");
        }

        return ariUtil.startMohAsync(session.getBridgeId())
                .chain(() -> transcribe(recording))
                .flatMap(text -> {
                    session.getGroupHandlingState().setTranscript(text);

                    if (text == null || text.isBlank()) {
                        return Uni.createFrom().nullItem();
                    }

                    logger.debug("Text input: {}", text);
                    long groupId = -1;

                    for (GroupListDto group : session.getGroupHandlingState().getGroupList()) {
                        if (text.replace(" ", "")
                                .toLowerCase()
                                .contains(group.getName().toLowerCase())) {
                            logger.debug("Match in group {}", group.getName());
                            groupId = group.getId();
                            break;
                        }
                    }

                    return groupService.getGroupById(groupId);
                }).invoke(group -> {
                    if (group != null) {
                        session.setSelectedGroup(group);
                    }
                }).replaceWithVoid();
    }

    @Override
    protected Uni<Void> handleRetryAsync(CallSession session) {
        Uni<Void> endMoh = Uni.createFrom()
                .voidItem()
                .eventually(() -> ariUtil.endMohAsync(session.getBridgeId()));

        if (session.getSelectedGroup() != null) {
            return endMoh;
        }

        final String userTranscriptSpeech = "group-user-transcript";

        return speechGenerationService.generateSpeech(
                        new CoquiRequest(
                                session.getGroupHandlingState().getTranscript(),
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
