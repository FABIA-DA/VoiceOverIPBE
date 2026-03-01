package at.htlleonding.fabia.callmgmt;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saves sessions to retrieve them later by their id.
 */
@ApplicationScoped
public final class SessionManager {
    private final Map<String, CallSession> sessions = new ConcurrentHashMap<>();

    /**
     * Gets a specific session by its id
     * @param bridgeId The id of the asterisk bridge
     * @return The call session.
     */
    public CallSession getSession(String bridgeId){
        return sessions.get(bridgeId);
    }

    /**
     * Saves a new session.
     * @param session The session to add
     */
    public void addSession(CallSession session){
        sessions.put(session.getBridgeId(), session);
    }

    /**
     * Deletes a session from the currently saved ones and returns it.
     * @param bridgeId The id of the bridge to be removed
     * @return The removed session
     */
    public CallSession removeSession(String bridgeId){
        return sessions.remove(bridgeId);
    }
}
