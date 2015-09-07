package models;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.avaje.ebean.Model;

import models.json.JsonSession;
import security.SecurityConstants;
import utils.DateTimeConstants;

/**
 * Allows for cached sessions to be persisted to be able to survive a container crash/restart.
 */
@Entity
@Table(name="sessions")
public class Session extends Model {
  
  /** The database ID for this session. */
  @Id
  private Long id;
  
  /** The user associated with this session. */
  @ManyToOne
  private User user;
  
  /** When was this session started? */
  private Date started;
  
  /** When is it expected for this session to expire? */
  private Date expires;
  
  /** The session key for this session. */
  @Column(name="session_key")
  private String key;
  
  /** Has this session expired yet? */
  private Boolean expired;
  
  
  public Session() {
    super();
  }
  
  
  /**
   * Constructor to build up session object properties from the given JSON object.
   * @param jsonSession
   */
  public Session(JsonSession jsonSession) {
    super();
    setId(jsonSession.id);
    if (jsonSession.user != null) {
      setUser(new User(jsonSession.user));
    }
    if (jsonSession.started != null) {
      try {
        setStarted(DateTimeConstants.DATETIME_FORMATTER.parse(jsonSession.started));
      } catch (ParseException e) {
        setStarted(null);
      }
    }
    if (jsonSession.expires != null) {
      try {
        setExpires(DateTimeConstants.DATETIME_FORMATTER.parse(jsonSession.expires));
      } catch (ParseException e) {
        setExpires(null);
      }
    }
    setKey(jsonSession.key);
    setExpired(jsonSession.expired);
  }
  
  
  
  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    String newline = System.getProperty("line.separator");
    
    buf.append("{"+newline);
    buf.append(String.format("  id      = %d%s", id, newline));
    buf.append(String.format("  user    = %s%s", user.toString(), newline));
    buf.append(String.format("  started = %s%s", (started != null) ? DateTimeConstants.DATETIME_FORMATTER.format(started) : "null", newline));
    buf.append(String.format("  expires = %s%s", (expires != null) ? DateTimeConstants.DATETIME_FORMATTER.format(expires) : "null", newline));
    buf.append(String.format("  key     = %s%s", key, newline));
    buf.append("}"+newline);
    
    return buf.toString();
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Date getStarted() {
    return started;
  }

  public void setStarted(Date started) {
    this.started = started;
  }

  public Date getExpires() {
    return expires;
  }

  public void setExpires(Date expires) {
    this.expires = expires;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }
  
  /**
   * Call this function to update the expiry date/time for this session.
   */
  public void touch() {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.SECOND, SecurityConstants.DEFAULT_SESSION_EXPIRY);

    // set the new session expiry date/time
    setExpires(cal.getTime());
  }

  public Boolean getExpired() {
    return expired;
  }

  public void setExpired(Boolean expired) {
    this.expired = expired;
  }

}
