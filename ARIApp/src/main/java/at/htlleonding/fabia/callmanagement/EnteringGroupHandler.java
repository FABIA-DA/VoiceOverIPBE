package at.htlleonding.fabia.callmanagement;

import at.htlleonding.fabia.AriContext;
import at.htlleonding.fabia.client.coquibe.SpeechGenerationClient;
import at.htlleonding.fabia.client.formbe.GroupClient;
import at.htlleonding.fabia.client.formbe.dtos.GroupListDto;
import ch.loway.oss.ari4java.ARI;
import ch.loway.oss.ari4java.tools.RestException;

import java.io.IOException;
import java.util.List;

@HandledState(CallState.EnteringGroup)
public final class EnteringGroupHandler implements CallStateHandler{
    @Override
    public void handle(CallSession session) {
        final String groupNamesSpeech = "group-names";

        try {
            ARI ari = AriContext.getInstance();
            ari.channels().play(session.getChannelId(), "sound:greeting");
            List<GroupListDto> list = GroupClient.getClient().getAllGroups();
            List<String> groupNames = list.stream().map(GroupListDto::getName).toList();
            String names = String.join(", ", groupNames);

            SpeechGenerationClient.getClient().generateSpeech(names, groupNamesSpeech);
            ari.channels().play(session.getChannelId(), "sound:" + groupNamesSpeech);
        } catch (RestException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
