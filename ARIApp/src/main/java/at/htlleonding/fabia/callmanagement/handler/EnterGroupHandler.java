package at.htlleonding.fabia.callmanagement.handler;

import at.htlleonding.fabia.callmanagement.CallSession;
import at.htlleonding.fabia.callmanagement.CallState;
import at.htlleonding.fabia.callmanagement.CallUtil;
import at.htlleonding.fabia.callmanagement.HandledState;
import at.htlleonding.fabia.client.coquibe.SpeechGenerationClient;
import at.htlleonding.fabia.client.formbe.GroupClient;
import at.htlleonding.fabia.client.formbe.dtos.GroupListDto;
import ch.loway.oss.ari4java.tools.RestException;

import java.io.IOException;
import java.util.List;

@HandledState(CallState.EnterGroup)
public final class EnterGroupHandler implements CallStateHandler {
    @Override
    public void handle(CallSession session) {
        final String groupsSpeechName = "group-names";

        try {
            List<GroupListDto> list = GroupClient.getClient().getAllGroups();
            String names = CallUtil.ConcatItems(list, GroupListDto::getName);

            SpeechGenerationClient.getClient().generateSpeech(names, groupsSpeechName);

            CallUtil.HandleGroups(session, groupsSpeechName);
        } catch (RestException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
