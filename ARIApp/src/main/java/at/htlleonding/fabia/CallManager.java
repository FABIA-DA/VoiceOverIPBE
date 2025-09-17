package at.htlleonding.fabia;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CallManager {
    private static CallManager callManager = null;
    private final Map<String, CallSession> sessions = new ConcurrentHashMap<String, CallSession>();

    public static CallManager getInstance() {
        if (callManager == null) {
            callManager = new CallManager();
        }
        return callManager;
    }

    public Map<String, CallSession> getSessions() {
        return sessions;
    }

    public CallSession getSession(String channelId){
        return sessions.get(channelId);
    }

    public void addSession(CallSession session){
        sessions.put(session.getChannelId(), session);
    }

    public void setCallState(String channelId, CallState state){
        CallSession session = sessions.get(channelId);
        session.setState(state);
    }
}
