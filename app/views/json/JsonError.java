package views.json;

import models.json.JsonObject;

/**
 * A generic class for reporting errors via JSON.
 */
public class JsonError extends JsonObject {

    /** The actual error message string. */
    public String error = null;

    public JsonError() {}

    public JsonError(String message) {
        this.error = message;
    }

}
