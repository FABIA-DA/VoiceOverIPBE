package at.htlleonding.fabia.callmgmt;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionManager {
    private static SessionManager manager = null;
    private final Map<String, CallSession> sessions = new ConcurrentHashMap<String, CallSession>();

    public static SessionManager getInstance() {
        if (manager == null) {
            manager = new SessionManager();
        }
        return manager;
    }

    public Collection<CallSession> getSessions() {
        return sessions.values();
    }

    public CallSession getSession(String channelId){
        return sessions.get(channelId);
    }

    public void addSession(CallSession session){
        sessions.put(session.getChannelId(), session);
    }

    public CallSession removeSession(String channelId){
        return sessions.remove(channelId);
    }
}
