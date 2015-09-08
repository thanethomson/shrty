package utils.jsonschema;

/**
 * An integer in the JSON schema.
 */
public class JsonSchemaInteger extends JsonSchema {

  public JsonSchemaType type = JsonSchemaType.INTEGER;
  public Integer multipleOf = null;
  public Integer maximum = null;
  public Integer minimum = null;
  public Integer _default = null;

}
