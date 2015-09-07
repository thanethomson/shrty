package models.json;

import models.Session;
import utils.DateTimeConstants;
import views.json.JsonUser;

/**
 * Object representation of a session.
 */
public class JsonSession extends JsonObject {
  
  public Long id = null;
  public JsonUser user = null;
  public String started = null;
  public String expires = null;
  public String key = null;
  public Boolean expired = null;

  public JsonSession() {}
  
  public JsonSession(Session session) {
    this.id = session.getId();
    this.user = (session.getUser() != null) ? new JsonUser(session.getUser()) : null;
    this.started = (session.getStarted() != null) ? DateTimeConstants.DATETIME_FORMATTER.format(session.getStarted()) : null;
    this.expires = (session.getExpires() != null) ? DateTimeConstants.DATETIME_FORMATTER.format(session.getExpires()) : null;
    this.key = session.getKey();
    this.expired = session.getExpired();
  }

}
