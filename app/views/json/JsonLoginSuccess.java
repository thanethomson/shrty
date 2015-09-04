package views.json;

import models.json.JsonObject;
import models.Session;

public class JsonLoginSuccess extends JsonObject {
  
  public String sessionId = null;

  public JsonLoginSuccess() {}
  
  public JsonLoginSuccess(Session session) {
    this.sessionId = session.getKey();
  }

}
