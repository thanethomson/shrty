package security;

import java.security.NoSuchAlgorithmException;

import play.mvc.Http;

/**
 * For handling session-related functionality.
 */
public interface SessionManager {

  /**
   * Attempts to create a new, unique session object, with an appropriate unique
   * session key.
   * @return The newly created session.
   */
  public Session create() throws NoSuchAlgorithmException;
  
  /**
   * Attempts to find the session with the given ID, without touching the session.
   * @param id The ID of the session to be fetched.
   * @return On success, a valid Session object. On failure, null.
   */
  public Session find(String id);

  /**
   * Saves the given session, updating its expiry date/time.
   * @param session The session to be saved.
   * @param httpSession The session from the incoming request context.
   * @return The supplied session.
   */
  public Session save(Session session, Http.Session httpSession);
  
  /**
   * Extracts the session from the given session and/or request object(s).
   * @param session The session object from the request context.
   * @param request The request object from the controller.
   * @return
   */
  public Session get(Http.Session session, Http.Request request);
  
  /**
   * Destroys the given session (e.g. for logging a user out, or invalidating a
   * session key).
   * @param session
   * @param httpSession
   */
  public void destroy(Session session, Http.Session httpSession);

}
