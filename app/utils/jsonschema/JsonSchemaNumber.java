package utils.jsonschema;

/**
 * A number in the JSON schema.
 */
public class JsonSchemaNumber extends JsonSchema {

    public JsonSchemaType type = JsonSchemaType.NUMBER;
    public Double multipleOf = null;
    public Double maximum = null;
    public Double minimum = null;
    public Double _default = null;
    
}
