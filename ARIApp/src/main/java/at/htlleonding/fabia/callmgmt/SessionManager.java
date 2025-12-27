package at.htlleonding.fabia.callmgmt;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saves sessions to retrieve them later by their id.
 */
@ApplicationScoped
public final class SessionManager {
    private final Map<String, CallSession> sessions = new ConcurrentHashMap<String, CallSession>();

    /**
     * Gets a specific session by its id
     * @param channelId The id of the asterisk channel
     * @return The call session.
     */
    public CallSession getSession(String channelId){
        return sessions.get(channelId);
    }

    /**
     * Saves a new session.
     * @param session The session to add
     */
    public void addSession(CallSession session){
        sessions.put(session.getChannelId(), session);
    }

    /**
     * Deletes a session from the currently saved ones and returns it.
     * @param channelId The id of the session to be removed
     * @return The removed session
     */
    public CallSession removeSession(String channelId){
        return sessions.remove(channelId);
    }
}
