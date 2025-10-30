package at.htlleonding.fabia.callmgmt.handler;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.audiomgmt.AudioItem;
import at.htlleonding.fabia.callmgmt.audiomgmt.PlaybackItem;
import at.htlleonding.fabia.callmgmt.audiomgmt.RecordingItem;
import at.htlleonding.fabia.callmgmt.util.CallState;
import at.htlleonding.fabia.callmgmt.util.Util;
import at.htlleonding.fabia.callmgmt.util.HandledState;
import at.htlleonding.fabia.client.coquibe.SpeechGenerationClient;
import at.htlleonding.fabia.client.formbe.GroupClient;
import at.htlleonding.fabia.client.formbe.dtos.Group;
import at.htlleonding.fabia.client.formbe.dtos.GroupListDto;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@HandledState(CallState.GROUP)
public final class GroupHandler extends StateHandler {
    public static final GroupHandler INSTANCE = new GroupHandler();

    private GroupHandler() {
    }

    @Override
    protected void handleList(CallSession session) throws IOException {
        String groupSpeech = "group-speech";

        List<GroupListDto> groupList = GroupClient.getClient().getAllGroups();

        if(groupList.isEmpty()){
            session.enqueueAudio(new PlaybackItem("groups_empty", session.getChannelId()));
            session.closeCall();
            return;
        }

        session.getGroupHandlingState().setGroupList(groupList);
        String names = Util.ConcatItems(groupList, GroupListDto::getName);
        SpeechGenerationClient.getClient().generateSpeech("Wir haben diese Gruppen zur Verfügung: " + names, groupSpeech);

        session.enqueueAudio(new PlaybackItem(groupSpeech, session.getChannelId()));
    }

    @Override
    protected void handleRequestInput(CallSession session) {
        session.enqueueAudio(new PlaybackItem("group_input_request", session.getChannelId()));
        RecordingItem recording = new RecordingItem(session.getChannelId());
        session.getGroupHandlingState().setRecording(recording);
        session.enqueueAudio(recording);
    }

    @Override
    protected void handleProcessInput(CallSession session) throws IOException {
        RecordingItem recording = session.getGroupHandlingState().getRecording();
        if (recording == null) {
            throw new IllegalStateException("No recording happened before input processing");
        }

        String text = transcribe(recording);

        for (GroupListDto group : session.getGroupHandlingState().getGroupList()) {
            Pattern pattern = Pattern.compile(group.getName(), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                Group selected = GroupClient.getClient().getGroupById(group.getId());
                session.setSelectedGroup(selected);
                break;
            }
        }
    }

    @Override
    protected void handleRetry(CallSession session) {
        if (session.getSelectedGroup() == null) {
            session.enqueueAudio(new PlaybackItem("group_not_found", session.getChannelId()));
            session.resetBaseState();
        }
    }
}
