package models.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The base class for all JSON objects.
 */
public abstract class JsonObject {

    /**
     * Converts this object to its JSON representation.
     * @return
     */
    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JsonNode toJsonNode() {
        return new ObjectMapper().valueToTree(this);
    }

}
