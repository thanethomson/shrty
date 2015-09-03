package repos;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

import com.avaje.ebean.Ebean;
import com.google.inject.Inject;

import crypto.HashProvider;
import exceptions.AlreadyExistsException;
import exceptions.DoesNotExistException;
import exceptions.InvalidPasswordException;
import models.User;
import play.Logger;
import play.mvc.Http;
import security.Session;
import security.SessionManager;

/**
 * Handles all user- and authentication-related functionality.
 */
public class AuthRepo {
  
  private static final Logger.ALogger logger = Logger.of(AuthRepo.class);
  private final SessionManager sessionManager;
  
  @Inject
  public AuthRepo(SessionManager sessionManager) {
    this.sessionManager = sessionManager;
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
   * Attempts to find the user with the given e-mail address.
   * @param email
   * @return
   */
  public User findUserByEmail(String email) {
    return Ebean.find(User.class).where().ilike("email", email).findUnique();
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
    Session session = sessionManager.create();
    session.setUser(user);
    sessionManager.save(session, httpSession);
    
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
    sessionManager.destroy(session, httpSession);
  }

}
