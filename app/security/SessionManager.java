package security;

/**
 * For handling session-related functionality.
 */
public interface SessionManager {

    /**
     * Attempts to find the session with the given ID, without touching the session.
     * @param id The ID of the session to be fetched.
     * @return On success, a valid Session object. On failure, null.
     */
    public Session find(String id);

    /**
     * Saves the given session, updating its expiry date/time.
     * @param session The session to be saved.
     * @return The supplied session.
     */
    public Session save(Session session);

}
