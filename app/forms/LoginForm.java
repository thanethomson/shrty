package forms;

import play.data.validation.Constraints.Required;

/**
 * The standard login form for logging into the Shrty administration interface.
 */
public class LoginForm {
  
  @Required
  private String email = null;
  @Required
  private String password = null;
  
  
  public LoginForm() {}
  
  public LoginForm(String email, String password) {
    setEmail(email);
    setPassword(password);
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
