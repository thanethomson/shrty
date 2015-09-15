package forms;

import models.json.JsonObject;
import play.data.validation.Constraints;

/**
 * Helps in validating signups via the API.
 */
public class SignupForm extends JsonObject {
  
  @Constraints.Required
  private String firstName = null;
  
  @Constraints.Required
  private String lastName = null;
  
  @Constraints.Required
  @Constraints.Email
  private String email = null;
  
  @Constraints.Required
  private String password = null;

  public SignupForm() {}
  
  public SignupForm(String firstName, String lastName, String email, String password) {
    setFirstName(firstName);
    setLastName(lastName);
    setEmail(email);
    setPassword(password);
  }
  
  

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
