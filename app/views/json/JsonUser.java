package views.json;

import models.User;
import models.json.JsonObject;
import utils.DateTimeConstants;

/**
 * JSON representation of a User object from the database.
 */
public class JsonUser extends JsonObject {
  
  public Long id = null;
  public String firstName = null;
  public String lastName = null;
  public String email = null;
  public String created = null;

  public JsonUser() {}
  
  public JsonUser(User user) {
    this.id = user.getId();
    this.firstName = user.getFirstName();
    this.lastName = user.getLastName();
    this.email = user.getEmail();
    this.created = DateTimeConstants.DATETIME_FORMATTER.format(user.getCreated());
  }

}
