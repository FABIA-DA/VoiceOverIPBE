package at.htlleonding.fabia;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class CallSession {
    private String channelId;
    private String channelName;
    private CallState state;

    public CallSession(String channelId, String channelName, CallState state) {
        this.channelId = channelId;
        this.channelName = channelName;
        this.state = state;
    }
}
