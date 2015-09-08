package utils.jsonschema;

/**
 * For JSON schema property/object types.
 */
public enum JsonSchemaType {
  ARRAY,
  BOOLEAN,
  INTEGER,
  NUMBER,
  NULL,
  OBJECT,
  STRING;

  @Override
  public String toString() {
    switch (this) {
      case ARRAY: return "Array";
      case BOOLEAN: return "Boolean";
      case INTEGER: return "Integer";
      case NUMBER: return "Number";
      case NULL: return "Null";
      case OBJECT: return "Object";
      case STRING: return "String";
      default: return "Unknown";
    }
  }
}
