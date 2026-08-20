package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.*;
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

    @Override
    protected Uni<Void> handleListAsync(CallSession session) {
        String groupSpeech = "group-speech";

        return ariUtil.startMohAsync(session.getChannelId())
                .chain(groupService::getAllGroups)
                .flatMap(groupList -> {
                    List<GroupListDto> groups = groupList.getGroups();

                    if (groups.isEmpty()) {
                        session.enqueue("empty-groups", "groups-empty");
                        session.goToGoodbye();
                        return Uni.createFrom().voidItem();
                    }

                    session.getGroupHandlingState().setGroupList(groups);
                    String names = Util.ConcatItems(groups, GroupListDto::getName);

                    return speechGenerationService.generateSpeech(new CoquiRequest(names, groupSpeech));
                })
                .invoke(() -> {
                    if (session.getState() == CallState.GOODBYE) {
                        return;
                    }

                    session.enqueue("group-intro", "form-group-preamble", groupSpeech);
                })
                .eventually(() -> ariUtil.endMohAsync(session.getChannelId()));
    }

    @Override
    protected Uni<Void> handleRequestInputAsync(CallSession session) {
        return Uni.createFrom().voidItem()
                .invoke(() -> {
                    session.enqueue("group-input-request", "group-input-request");
                    session.getGroupHandlingState().setRecording(session.enqueue());
                });
    }

    @Override
    protected Uni<Void> handleProcessInputAsync(CallSession session) {
        RecordingItem recording = session.getGroupHandlingState().getRecording();
        if (recording == null) {
            logger.error("No recording happened before input processing");
            session.enqueue("group-recording-null", "error");
            session.goToGoodbye();
            return Uni.createFrom().voidItem();
        }

        return ariUtil.startMohAsync(session.getChannelId())
                .chain(() -> transcribe(recording))
                .flatMap(text -> {
                    if (text == null || text.isBlank()) {
                        return Uni.createFrom().nullItem();
                    }

                    session.getGroupHandlingState().setTranscript(text);
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

                    if (groupId == -1) {
                        return Uni.createFrom().nullItem();
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
                .eventually(() -> ariUtil.endMohAsync(session.getChannelId()));

        if (session.getSelectedGroup() != null) {
            return endMoh;
        }

        session.resetBaseState();

        String transcript = session.getGroupHandlingState().getTranscript();
        if (transcript == null || transcript.isBlank()) {
            session.enqueue("could-not-find-transcript", "could-not-understand");
            return endMoh;
        }

        final String userTranscriptSpeech = "group-user-transcript";

        return speechGenerationService.generateSpeech(
                        new CoquiRequest(
                                session.getGroupHandlingState().getTranscript(),
                                userTranscriptSpeech))
                .invoke(() -> {
                    session.enqueue("group-check", "we-understood", userTranscriptSpeech);
                })
                .eventually(() -> endMoh);
    }
}
