package models.json;

/**
 * For signing a person up via the API.
 */
public class JsonSignup extends JsonObject {
  
  public String firstName = null;
  public String lastName = null;
  public String email = null;
  public String password = null;

  public JsonSignup() {}
  
  public JsonSignup(String firstName, String lastName, String email, String password) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
  }

}
