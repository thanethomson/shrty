package utils.jsonschema;

import java.util.List;
import java.util.Map;

/**
 * An instance of an object in a JSON schema.
 */
public class JsonSchemaObject extends JsonSchema {

    public JsonSchemaType type = JsonSchemaType.OBJECT;
    /** The required properties for this object. */
    public List<String> required = null;
    /** Properties for this object. */
    public Map<String, JsonSchema> properties = null;
    /** The maximum number of properties permissible for this object. */
    public Integer maxProperties = null;
    /** The minimum number of properties permissible for this object. */
    public Integer minProperties = null;
    
}
