package utils.jsonschema;

/**
 * A string in a JSON schema.
 */
public class JsonSchemaString extends JsonSchema {

    public JsonSchemaType type = JsonSchemaType.STRING;
    public Integer maxLength = null;
    public Integer minLength = null;
    /** If there is a particular regular expression pattern to which this property should conform. */
    public String pattern = null;
    public String _default = null;
    
}
