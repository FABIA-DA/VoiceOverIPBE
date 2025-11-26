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
import at.htlleonding.fabia.client.formbe.dtos.Group;
import at.htlleonding.fabia.client.formbe.dtos.GroupListDto;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    protected void handleList(CallSession session) {
        String groupSpeech = "group-speech";

        List<GroupListDto> groupList = groupService.getAllGroups()
                .await()
                .indefinitely()
                .getGroups();

        if (groupList.isEmpty()) {
            session.enqueueAudio(new PlaybackItem("groups_empty", session.getChannelId(), ariUtil, activeAudioRegistry));
            session.closeCall();
            return;
        }

        session.getGroupHandlingState().setGroupList(groupList);
        String names = Util.ConcatItems(groupList, GroupListDto::getName);
        speechGenerationService.generateSpeech(new CoquiRequest("Wir haben diese Formularkategorien zur Verfügung: " + names, groupSpeech))
                .await()
                .indefinitely();
        session.enqueueAudio(new PlaybackItem(groupSpeech, session.getChannelId(), ariUtil, activeAudioRegistry));

    }

    @Override
    protected void handleRequestInput(CallSession session) {
        session.enqueueAudio(new PlaybackItem("group_input_request", session.getChannelId(), ariUtil, activeAudioRegistry));
        RecordingItem recording = new RecordingItem(session.getChannelId(), ariUtil, activeAudioRegistry);
        session.getGroupHandlingState().setRecording(recording);
        session.enqueueAudio(recording);
    }

    @Override
    protected void handleProcessInput(CallSession session) {
        RecordingItem recording = session.getGroupHandlingState().getRecording();
        if (recording == null) {
            throw new IllegalStateException("No recording happened before input processing");
        }

        String text = transcribe(recording).await().indefinitely();

        if (text == null) {
            return;
        }

        for (GroupListDto group : session.getGroupHandlingState().getGroupList()) {
            Pattern pattern = Pattern.compile(group.getName(), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                Group selected = groupService.getGroupById(group.getId())
                        .await()
                        .indefinitely();
                session.setSelectedGroup(selected);
                break;
            }
        }
    }

    @Override
    protected void handleRetry(CallSession session) {
        if (session.getSelectedGroup() == null) {
            session.enqueueAudio(new PlaybackItem("group_not_found", session.getChannelId(), ariUtil, activeAudioRegistry));
            session.resetBaseState();
        }
    }
}
