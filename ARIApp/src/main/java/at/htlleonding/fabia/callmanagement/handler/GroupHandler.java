package at.htlleonding.fabia.callmanagement.handler;

import at.htlleonding.fabia.callmanagement.*;
import at.htlleonding.fabia.client.coquibe.SpeechGenerationClient;
import at.htlleonding.fabia.client.formbe.GroupClient;
import at.htlleonding.fabia.client.formbe.dtos.Group;
import at.htlleonding.fabia.client.formbe.dtos.GroupListDto;
import at.htlleonding.fabia.client.whisperbe.TranscriptionClient;

import java.io.IOException;
import java.util.List;

@HandledState(CallState.Group)
public final class GroupHandler extends StateHandler {
    private List<GroupListDto> groupList = null;
    private RecordingItem recordingItem = null;

    @Override
    protected CallState nextState () {
        return CallState.Form;
    }

    @Override
    protected void handleList (CallSession session) throws IOException {
        System.out.println("Handling group list");
        String groupsSpeechName = "group-names";
        groupList = GroupClient.getClient().getAllGroups();
        String names = CallUtil.ConcatItems(groupList, GroupListDto::getName);
        System.out.println("Groups: " + names);
        SpeechGenerationClient.getClient().generateSpeech("Wir haben diese Gruppen zur Verfügung: " + names, groupsSpeechName);


        //session.enqueueAudio(new PlaybackItem("group_list", session.getChannelId()));
        session.enqueueAudio(new PlaybackItem(groupsSpeechName, session.getChannelId()));
    }

    @Override
    protected void handleRequestInput (CallSession session){
        System.out.println("Handling group request input");
        session.enqueueAudio(new PlaybackItem("group_input_request", session.getChannelId()));
        recordingItem = new RecordingItem(session.getChannelId());
        session.enqueueAudio(recordingItem);
    }

    @Override
    protected void handleProcessInput (CallSession session) throws IOException {
        System.out.println("Handling group process input");
        if (recordingItem == null) {
            return;
        }

        String text = TranscriptionClient.getClient().transcribe(recordingItem.getName());

        if (text == null) {
            return;
        }

        for (GroupListDto group : groupList) {
            if (group.getName().trim().equalsIgnoreCase(text.trim())) {
                Group selected = GroupClient.getClient().getGroupById(group.getId());
                session.setSelectedGroup(selected);
                break;
            }
        }
        session.setState(CallState.Form);
    }
}
