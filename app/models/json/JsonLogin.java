package models.json;

public class JsonLogin extends JsonObject {
  
  public String email = null;
  public String password = null;

  public JsonLogin() {}
  
  public JsonLogin(String email, String password) {
    this.email = email;
    this.password = password;
  }

}
