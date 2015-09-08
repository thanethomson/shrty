package utils.jsonschema;

/**
 * We implement a sub-set of the JSON schema specification.
 * See http://json-schema.org/
 */
public class JsonSchema {

    /** The title for this schema/property. */
    public String title = null;
    /** A description for this schema/property. */
    public String description = null;
    /** What _type of schema is this? */
    public JsonSchemaType _type = null;
    /** For properties. */
    public Boolean required = false;
    
    public JsonSchema() {}
    
}
