package views.json;

import models.json.JsonObject;

public class JsonGenericMessage extends JsonObject {
  
  public String message = null;

  public JsonGenericMessage() {}
  
  public JsonGenericMessage(String message) {
    this.message = message;
  }

}
