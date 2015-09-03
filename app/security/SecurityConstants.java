package security;

/**
 * Constants related to system security.
 */
public class SecurityConstants {

  /** The default number of seconds after which the session expires after no activity. */
  public final static Integer DEFAULT_SESSION_EXPIRY = 60*60;
  public final static String SESSIONKEY_HEADER = "X-Session-ID";
  public final static String COOKIE_SESSION_ID = "sessionId";
  /**
   * BEWARE: If you change this, you will change the whole way that short codes are generated!
   */
  public final static String SHORTCODE_HASH_SALT = "sPdw6CA6+QcYoDVGSKHHLrQBMbD8M0qdB+fGYYyTCZ8=";
  public final static Integer SHORTCODE_LENGTH = 5;
  public final static String SHORTCODE_CHARSET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
  
}
