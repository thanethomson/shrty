package security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import com.google.inject.Inject;

import crypto.HashProvider;
import play.Logger;
import play.cache.CacheApi;
import play.mvc.Http;
import utils.DateTimeConstants;

/**
 * The primary session manager for Shrty - the EHCache-based one.
 */
public class CacheSessionManager implements SessionManager {

  private static final Logger.ALogger logger = Logger.of(CacheSessionManager.class);
  private final CacheApi cache;

  @Inject
  public CacheSessionManager(CacheApi cache) {
    this.cache = cache;
  }
  
  @Override
  public Session create() throws NoSuchAlgorithmException {
    Session session = new Session();
    String input, key;
    SecureRandom random = new SecureRandom();
    String time = DateTimeConstants.DATETIME_FORMATTER.format(new Date());
    
    // try to generate a new session key
    while (session.getKey() == null) {
      input = String.format("%s-%d", time, random.nextInt(10000000));
      key = HashProvider.base64HashOf(input);
      logger.debug(String.format("Checking key: %s", key));
      // check if the key exists
      if (find(key) == null) {
        // if not, we have a winner!
        session.setKey(key);
      }
    }
    
    // set the session starting date/time
    session.setStarted(new Date());
    
    logger.debug(String.format("Created session with ID: %s", session.getKey()));
    
    return session;
  }

  @Override
  public Session find(String id) {
    return cache.getOrElse(String.format("session.%s", id), () -> null);
  }

  @Override
  public Session save(Session session, Http.Session httpSession) {
    logger.debug(String.format("Updating session with ID: %s", session.getKey()));
    // update its expiry date/time
    session.touch();
    cache.set(String.format("session.%s", session.getKey()), session, SecurityConstants.DEFAULT_SESSION_EXPIRY);
    // store the session ID
    httpSession.put(SecurityConstants.COOKIE_SESSION_ID, session.getKey());
    return session;
  }
  
  @Override
  public Session get(Http.Session session, Http.Request request) {
    Session result = null;
    String sessionId = null;
    
    if (session.containsKey(SecurityConstants.COOKIE_SESSION_ID))
      sessionId = session.get(SecurityConstants.COOKIE_SESSION_ID);
    else if (request.hasHeader(SecurityConstants.SESSIONKEY_HEADER))
      sessionId = request.getHeader(SecurityConstants.SESSIONKEY_HEADER);
    
    if (sessionId != null) {
      logger.debug(String.format("Found session ID in incoming session/request: %s", sessionId));
      // try to look up the session
      result = find(sessionId);
      if (result == null) {
        logger.error(String.format("Session with ID %s does not exist", sessionId));
      }
    }
    
    return result;
  }

  @Override
  public void destroy(Session session, Http.Session httpSession) {
    Session found = find(session.getKey());
    
    if (found != null) {
      logger.debug(String.format("Removing session with key %s from cache", found.getKey()));
      // remove the relevant entry from the cache
      cache.remove(String.format("session.%s", found.getKey()));
      httpSession.remove(SecurityConstants.COOKIE_SESSION_ID);
    } else {
      logger.debug(String.format("Session with ID %s does not exist or already removed from cache", session.getKey()));
    }
  }

}
