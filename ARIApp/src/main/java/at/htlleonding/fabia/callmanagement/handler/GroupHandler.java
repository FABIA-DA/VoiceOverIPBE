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
public final class GroupHandler implements CallStateHandler {
    private List<GroupListDto> groupList = null;
    private RecordingItem recordingItem = null;

    @Override
    public void handle(CallSession session) {
        switch (session.getCurrentBaseState()) {
            case BaseState.List -> {
                System.out.println("Handling group list");
                try {
                    String groupsSpeechName = "group-names";
                    if (groupList == null) {
                        groupList = GroupClient.getClient().getAllGroups();
                        String names = CallUtil.ConcatItems(groupList, GroupListDto::getName);
                        SpeechGenerationClient.getClient().generateSpeech(names, groupsSpeechName);
                    }

                    session.enqueueAudio(new PlaybackItem("group_list", session.getChannelId()));
                    session.enqueueAudio(new PlaybackItem(groupsSpeechName, session.getChannelId()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case BaseState.RequestInput -> {
                System.out.println("Handling group request input");
                session.enqueueAudio(new PlaybackItem("group_input_request", session.getChannelId()));
                recordingItem = new RecordingItem(session.getChannelId());
                session.enqueueAudio(recordingItem);
            }
            case BaseState.ProcessInput -> {
                System.out.println("Handling group process input");
                if (recordingItem == null) {
                    return;
                }

                try {
                    String text = TranscriptionClient.getClient().transcribe(recordingItem.getName());

                    if(text == null){
                        return;
                    }

                    for(GroupListDto group : groupList){
                        if(group.getName().trim().equalsIgnoreCase(text.trim())){
                            Group selected = GroupClient.getClient().getGroupById(group.getId());
                            session.setSelectedGroup(selected);
                            break;
                        }
                    }
                    session.setState(CallState.Form);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        session.nextAudioOrStep();
    }
}
