package utils.jsonschema;

import java.util.List;

/**
 * An array in the JSON schema.
 */
public class JsonSchemaArray extends JsonSchema {

    public JsonSchemaType type = JsonSchemaType.ARRAY;
    /** The maximum number of items in this array. */
    public Integer maxItems = null;
    /** The minimum number of items in this array. */
    public Integer minItems = null;
    /** Should all of the items in this array be unique? */
    public Boolean uniqueItems = null;
    /** The default values for this array, if not specified. */
    public List<Object> _default = null;
    
}
