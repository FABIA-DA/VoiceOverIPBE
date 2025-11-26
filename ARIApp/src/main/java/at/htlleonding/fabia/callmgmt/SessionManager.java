package at.htlleonding.fabia.callmgmt;

import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public final class SessionManager {
    private final Map<String, CallSession> sessions = new ConcurrentHashMap<String, CallSession>();

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
