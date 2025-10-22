package at.htlleonding.fabia.callmanagement;

import at.htlleonding.fabia.client.formbe.dtos.Form;
import at.htlleonding.fabia.client.formbe.dtos.Group;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Queue;

public final class CallSession {
    @Getter
    private final String channelId;
    @Getter
    private final String channelName;
    @Getter
    @Setter
    private CallState state;
    @Getter
    @Setter
    private Group selectedGroup;
    @Getter
    @Setter
    private Form selectedForm;
    @Getter
    @Setter
    private Integer currentFieldGroupIdx;
    @Getter
    @Setter
    private Integer currentSingleChoiceFieldIdx;
    @Getter
    @Setter
    private Integer currentFieldIdx;
    @Getter
    @Setter
    private String currentRecordingName;
    @Getter
    private final Queue<String> audioQueue = new LinkedList<>();

    public CallSession(String channelId, String channelName, CallState initialState) {
        this.channelId = channelId;
        this.channelName = channelName;
        this.state = initialState;
    }
}
