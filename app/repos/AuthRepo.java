package repos;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.QueryIterator;
import com.google.inject.Inject;

import caching.CacheManager;
import crypto.HashProvider;
import exceptions.AlreadyExistsException;
import exceptions.DoesNotExistException;
import exceptions.InvalidPasswordException;
import models.User;
import play.Logger;
import play.mvc.Http;
import models.Session;
import security.SecurityConstants;
import utils.DateTimeConstants;

/**
 * Handles all user- and authentication-related functionality.
 */
public class AuthRepo {
  
  private static final Logger.ALogger logger = Logger.of(AuthRepo.class);
  private final CacheManager cacheManager;
  
  @Inject
  public AuthRepo(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }
  
  /**
   * Provides an interface to create a new user in the system.
   * @param firstName
   * @param lastName
   * @param email
   * @param password
   * @return
   * @throws NoSuchAlgorithmException
   * @throws AlreadyExistsException 
   */
  public User createUser(String firstName, String lastName, String email, String password) throws NoSuchAlgorithmException, AlreadyExistsException {
    logger.debug(String.format("Attempting to create new user with e-mail address: %s", email));
    
    // first check if that e-mail address has already been registered
    if (Ebean.find(User.class).where().ilike("email", email).findUnique() != null)
      throw new AlreadyExistsException(String.format("User with e-mail address %s already exists in database", email));
    
    User user = new User();
    
    // set up the new user's details
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    user.setPasswordHash(getPasswordHash(password));
    user.setCreated(new Date());
    
    // save the user to the database
    Ebean.save(user);
    
    return user;
  }
  
  
  /**
   * Allows one to generate a unique session key.
   * @return A string containing the new session key.
   * @throws NoSuchAlgorithmException 
   */
  protected String generateSessionKey() throws NoSuchAlgorithmException {
    String input, key = null;
    SecureRandom random = new SecureRandom();
    String time = DateTimeConstants.DATETIME_FORMATTER.format(new Date());
    
    // try to generate a new session key
    while (key == null) {
      input = String.format("%s-%d", time, random.nextInt(10000000));
      key = HashProvider.base64HashOf(input);
      // check if the key exists
      if (getSession(key) != null)
        // not found yet
        key = null;
    }
    
    return key;
  }
  
  /**
   * Attempts to create a new session for the given user.
   * @param user
   * @return
   * @throws NoSuchAlgorithmException 
   */
  public Session createSession(User user) throws NoSuchAlgorithmException {
    // try to find an open session for this user (i.e. recycle old sessions)
    Session session = findOpenSession(user);
    
    // if we couldn't find an open session, create one
    if (session == null) {
      session = new Session();
      session.setUser(user);
      session.setStarted(new Date());
      session.setKey(generateSessionKey());
      session.setExpired(false);
    }
    // set the (new) expiry date/time
    session.touch();
    
    // save the session to the database
    Ebean.save(session);
    // save the session to the cache
    cacheSession(session);
    
    logger.debug(String.format("Created new session %s for user %s", session.getKey(), user.getEmail()));
    
    return session;
  }
  
  
  /**
   * Terminates the given session.
   * @param session
   */
  public void endSession(Session session) {
    logger.debug(String.format("Ending session %s", session.getKey()));
    // make sure we remove the session from the cache
    uncacheSession(session);
    // we need the version of the session loaded from the database - can't trust the cache
    session = findSessionById(session.getId());
    // mark it as having expired
    session.setExpired(true);
    Ebean.save(session);
  }
  
  
  /**
   * Attempts to find the session with the given database ID.
   * @param id The database ID of the session.
   * @return A Session object if found, or null otherwise.
   */
  public Session findSessionById(long id) {
    return Ebean.find(Session.class).where().idEq(id).findUnique();
  }
  
  
  /**
   * Runs through all sessions in the database that look like they should have
   * expired, and expires them if necessary.
   * @return
   */
  public int checkExpiredSessions() {
    int updateCount = 0;
    logger.debug("Attempting to check for expired sessions in the database...");
    
    Ebean.beginTransaction();
    Date now = new Date();
    
    try {
      QueryIterator<Session> iterator = Ebean.find(Session.class)
          .where()
            .eq("expired", false)
            .lt("expires", now)
          .findIterate();
      Session session, cached;
      
      try {
        while (iterator.hasNext()) {
          session = iterator.next(); 
          
          // if we have a cached version of this session
          cached = getCachedSession(session.getKey());
          if (cached != null && cached.getExpires().before(now)) {
            // update the stored expiry date of the session if it's different to the cached version
            if (cached.getExpires().compareTo(session.getExpires()) == 0) {
              session.setExpires(cached.getExpires());
              Ebean.save(session);
            }
          } else {
            logger.debug(String.format("Session with key %s has now expired", session.getKey()));
            endSession(session);
          }
        }
      } finally {
        iterator.close();
      }
      
    } finally {
      Ebean.endTransaction();
    }
    
    logger.debug(String.format("Updated %d entries in session database", updateCount));
    return updateCount;
  }
  
  
  /**
   * Stores the given session in the cache for later retrieval.
   * @param session
   */
  public void cacheSession(Session session) {
    // store the session in the cache, for a limited time
    cacheManager.storeSession(session);
    logger.debug(String.format("Added session %s to cache", session.getKey()));
  }
  
  
  /**
   * If the specified session exists in the cache, this function will remove it.
   * @param session
   */
  public void uncacheSession(Session session) {
    // try to find the cached session
    Session cached = getCachedSession(session.getKey());
    
    if (cached != null) {
      cacheManager.removeSession(cached);
      logger.debug(String.format("Removed session %s from cache", session.getKey()));
    }
  }
  
  /**
   * Attempts to retrieve the cached session associated with the specified key.
   * @param key
   * @return A Session object, if found; null otherwise.
   */
  public Session getCachedSession(String key) {
    return cacheManager.findSession(key);
  }
  
  
  /**
   * Tries to look up the session with the given key. First looks in the cache, and if
   * not found, tries to look it up from the database. Automatically caches it if it
   * finds the session in the database.
   * @param key The session key.
   * @return A Session object on success, or null if not found.
   */
  public Session getSession(String key) {
    Session session = getCachedSession(key);
    
    // if we can't find it in the cache
    if (session == null) {
      // try find it in the database
      session = findSessionByKey(key);
      if (session != null) {
        // cache it!
        cacheSession(session);
      }
    }
    
    return session;
  }
  
  
  /**
   * Updates the given session's expiry details in the cache. Assumes that persistence of
   * sessions to the database will be handled elsewhere.
   * @param session The session to update.
   * @return The newly updated session with new expiry date.
   */
  public Session touchSession(Session session) {
    Session cached = getCachedSession(session.getKey());
    
    // update the session expiry details
    cached.touch();
    // save it back to the cache
    cacheSession(cached);
    
    return cached;
  }
  
  
  /**
   * Attempts to find the first still-open session for the given user.
   * @param user
   * @return
   */
  public Session findOpenSession(User user) {
    return Ebean.find(Session.class)
        .where()
          .eq("user", user)            // for this user
          .eq("expired", false)        // non-expired sessions
          .gt("expires", new Date())   // whose expiry date is in the future
        .orderBy("expires desc")       // order the sessions by their expiry date with latest first
        .setMaxRows(1)                 // we only need 1
        .findUnique();
  }
  
  
  /**
   * Attempts to look for unexpired sessions by way of their key in the database.
   * @param key
   * @return A Session object if found, or null otherwise.
   */
  public Session findSessionByKey(String key) {
    return Ebean.find(Session.class)
        .where()
          .eq("key", key)
          .eq("expired", false)
        .findUnique();
  }
  
  
  /**
   * Attempts to find the user with the given e-mail address.
   * @param email
   * @return
   */
  public User findUserByEmail(String email) {
    return Ebean.find(User.class).where().ilike("email", email).findUnique();
  }
  
  
  /**
   * Attempts to find the user with the given ID.
   * @param id
   * @return
   */
  public User findUserById(int id) {
    return Ebean.find(User.class).where().idEq(id).findUnique();
  }
  
  
  /**
   * Globally accessible function to obtain the password hash for a given password.
   * @param password
   * @return
   * @throws NoSuchAlgorithmException 
   */
  public static String getPasswordHash(String password) throws NoSuchAlgorithmException {
    return HashProvider.base64HashOf(password);
  }
  
  
  /**
   * Attempts to perform a login operation with the given e-mail address and password.
   * @param email The e-mail address of the user logging in.
   * @param password The cleartext password of the user logging in.
   * @param httpSession The incoming request context's session.
   * @return A new session object.
   * @throws NoSuchAlgorithmException
   * @throws DoesNotExistException If the user with the given e-mail address does not exist.
   * @throws InvalidPasswordException If the supplied password is invalid.
   */
  public Session login(String email, String password, Http.Session httpSession) throws NoSuchAlgorithmException, DoesNotExistException, InvalidPasswordException {
    // first try to authenticate the user
    User user = findUserByEmail(email);
    
    // no such user
    if (user == null)
      throw new DoesNotExistException(String.format("User with e-mail address %s does not exist", email));
    
    // now check the user's password
    String passwordHash = getPasswordHash(password);
    
    // access denied - invalid password
    if (!passwordHash.equals(user.getPasswordHash()))
      throw new InvalidPasswordException(String.format("Invalid password for user with e-mail address %s", email));
    
    // create and set up the session
    Session session = createSession(user);
    httpSession.put(SecurityConstants.COOKIE_SESSION_ID, session.getKey());
    
    logger.debug(String.format("New session created for user %s: %s", email, session.getKey()));
    
    return session;
  }
  
  
  /**
   * Terminates the given session.
   * @param session
   * @param httpSession
   */
  public void logout(Session session, Http.Session httpSession) {
    logger.debug(String.format("Logging user %s with session key %s out", session.getUser().getEmail(), session.getKey()));
    endSession(session);
    if (httpSession.containsKey(SecurityConstants.COOKIE_SESSION_ID))
      httpSession.remove(SecurityConstants.COOKIE_SESSION_ID);
  }
  
  
  /**
   * Attempts to look up the relevant session from the given HTTP session and request information.
   * @param httpSession
   * @param httpRequest
   * @return
   */
  public Session getSessionFromRequest(Http.Session httpSession, Http.Request httpRequest) {
    String sessionId = null;
    Session session = null;
    
    if (httpSession.containsKey(SecurityConstants.COOKIE_SESSION_ID))
      sessionId = httpSession.get(SecurityConstants.COOKIE_SESSION_ID);
    else if (httpRequest.hasHeader(SecurityConstants.SESSIONKEY_HEADER))
      sessionId = httpRequest.getHeader(SecurityConstants.SESSIONKEY_HEADER);
    
    if (sessionId != null)
      session = getSession(sessionId);
    
    return session;
  }

}
