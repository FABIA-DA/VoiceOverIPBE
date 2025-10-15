package at.htlleonding.fabia.callmanagement;

import at.htlleonding.fabia.client.formbe.dtos.Form;
import at.htlleonding.fabia.client.formbe.dtos.Group;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Queue;

@Getter
@Setter
public final class CallSession {
    private String channelId;
    private String channelName;
    private CallState state;
    private Group selectedGroup;
    private Form selectedForm;
    private Integer currentFieldGroupIdx;
    private Integer currentSingleChoiceFieldIdx;
    private Integer currentFieldIdx;
    private Queue<String> audioQueue = new LinkedList<>();

    public CallSession(String channelId, String channelName, CallState state) {
        this.channelId = channelId;
        this.channelName = channelName;
        this.state = state;
    }
}
