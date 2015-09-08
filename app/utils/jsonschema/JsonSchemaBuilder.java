package utils.jsonschema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import play.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Allows one to obtain JSON schema details from an input JSON string.
 */
public class JsonSchemaBuilder {

  private JsonSchema _schema = null;
  private static final Logger.ALogger logger = Logger.of(JsonSchemaBuilder.class);

  public JsonSchemaBuilder(String jsonInput) throws JsonProcessingException, IOException, JsonSchemaTypeException {
    logger.debug(String.format("Attempting to parse schema from incoming JSON:\n%s", jsonInput));
    buildSchema(jsonInput);
  }

  public JsonSchema getSchema() {
    return _schema;
  }

  private void buildSchema(String jsonInput) throws JsonProcessingException, IOException, JsonSchemaTypeException {
    JsonNode root = new ObjectMapper().readTree(jsonInput);
    _schema = buildSchema(root);
  }

  private JsonSchema buildSchema(JsonNode input) throws JsonSchemaTypeException {
    JsonSchema schema;
    JsonSchemaType schemaType = null;
    if (input.hasNonNull("type"))
      schemaType = lookupType(input.get("type").asText());
    else
      throw new JsonSchemaTypeException("Missing JSON schema _type");

    logger.debug(String.format("Attempting to build schema for object of _type %s", schemaType));

    switch (schemaType) {
      case ARRAY:
        schema = buildArraySchema(input, new JsonSchemaArray());
        break;
      case NUMBER:
        schema = buildNumberSchema(input, new JsonSchemaNumber());
        break;
      case INTEGER:
        schema = buildIntegerSchema(input, new JsonSchemaInteger());
        break;
      case OBJECT:
        schema = buildObjectSchema(input, new JsonSchemaObject());
        break;
      case STRING:
        schema = buildStringSchema(input, new JsonSchemaString());
        break;
      case BOOLEAN:
        schema = buildBooleanSchema(input, new JsonSchemaBoolean());
        break;
      default:
        throw new JsonSchemaTypeException("Cannot establish JSON schema _type for object");
    }
    // add the common attributes
    return buildCommonSchema(input, schema);
  }

  private JsonSchema buildCommonSchema(JsonNode input, JsonSchema schema) throws JsonSchemaTypeException {
    logger.debug("Adding common schema attributes");
    if (input.hasNonNull("title"))
      schema.title = input.get("title").asText();
    if (input.hasNonNull("description"))
      schema.description = input.get("description").asText();
    if (input.hasNonNull("type"))
      schema._type = lookupType(input.get("type").asText());

    // set the required fields
    if (schema instanceof JsonSchemaObject) {
      JsonSchemaObject obj = (JsonSchemaObject)schema;
      if (obj.required != null) {
        for (String requiredField: obj.required) {
          if (obj.properties.containsKey(requiredField)) {
            obj.properties.get(requiredField).required = true;
          }
        }
      }
    }

    return schema;
  }

  private JsonSchemaArray buildArraySchema(JsonNode input, JsonSchemaArray schema) throws JsonSchemaTypeException {
    logger.debug(String.format("Building array schema for node: %s", input.toString()));
    if (input.hasNonNull("maxItems"))
      schema.maxItems = input.get("maxItems").asInt();
    if (input.hasNonNull("minItems"))
      schema.minItems = input.get("minItems").asInt();
    if (input.hasNonNull("uniqueItems"))
      schema.uniqueItems = input.get("uniqueItems").asBoolean();
    if (input.hasNonNull("default")) {
      if (!input.get("default").isArray())
        throw new JsonSchemaTypeException("Was expecting array for default value for node");
      schema._default = new ArrayList<Object>();
      JsonNode el;
      Iterator<JsonNode> it = input.get("default").elements();
      while (it.hasNext()) {
        el = it.next();
        if (el.isInt() || el.isBigInteger())
          schema._default.add(el.asInt());
        else if (el.isDouble() || el.isFloat() || el.isFloatingPointNumber() || el.isNumber() || el.isBigDecimal())
          schema._default.add(el.asDouble());
        else if (el.isBoolean())
          schema._default.add(el.asBoolean());
        else if (el.isTextual())
          schema._default.add(el.asText());
      }
    }
    if (input.hasNonNull("items")) {
      if (input.get("items").hasNonNull("$ref")) {
        schema.itemSchema = input.get("items").get("$ref").asText();
        // strip off any preceding # characters
        if (schema.itemSchema.startsWith("#") && schema.itemSchema.length() > 1)
          schema.itemSchema = schema.itemSchema.substring(1);
      }
    }
    return schema;
  }

  private JsonSchemaBoolean buildBooleanSchema(JsonNode input, JsonSchemaBoolean schema) {
    logger.debug(String.format("Building boolean schema for node: %s", input.toString()));
    if (input.hasNonNull("default"))
      schema._default = input.get("default").asBoolean();
    return schema;
  }

  private JsonSchemaInteger buildIntegerSchema(JsonNode input, JsonSchemaInteger schema) {
    logger.debug(String.format("Building object schema for node: %s", input.toString()));
    if (input.hasNonNull("multipleOf"))
      schema.multipleOf = input.get("multipleOf").asInt();
    if (input.hasNonNull("maximum"))
      schema.maximum = input.get("maximum").asInt();
    if (input.hasNonNull("minimum"))
      schema.minimum = input.get("minimum").asInt();
    if (input.hasNonNull("default"))
      schema._default = input.get("default").asInt();
    return schema;
  }

  private JsonSchemaNumber buildNumberSchema(JsonNode input, JsonSchemaNumber schema) {
    logger.debug(String.format("Building number schema for node: %s", input.toString()));
    if (input.hasNonNull("multipleOf"))
      schema.multipleOf = input.get("multipleOf").asDouble();
    if (input.hasNonNull("maximum"))
      schema.maximum = input.get("maximum").asDouble();
    if (input.hasNonNull("minimum"))
      schema.minimum = input.get("minimum").asDouble();
    if (input.hasNonNull("default"))
      schema._default = input.get("default").asDouble();
    return schema;
  }

  private JsonSchemaObject buildObjectSchema(JsonNode input, JsonSchemaObject schema) throws JsonSchemaTypeException {
    JsonNode el;

    logger.debug(String.format("Building object schema for node: %s", input.toString()));

    // if this schema refers to another schema
    if (input.hasNonNull("$ref")) {
      schema.ref = input.get("$ref").asText();
      if (schema.ref.startsWith("#"))
        schema.ref = schema.ref.substring(1);
    } else {
      if (input.hasNonNull("required")) {
        schema.required = new ArrayList<String>();
        if (!input.get("required").isArray())
          throw new JsonSchemaTypeException("Expecting array for required property");
        logger.debug(String.format("Adding required fields: %s", input.get("required")));
        Iterator<JsonNode> it = input.get("required").elements();
        while (it.hasNext()) {
          el = it.next();
          logger.debug(String.format("Adding required field: %s", el.asText()));
          schema.required.add(el.asText());
        }
      }
      if (input.hasNonNull("properties") && input.get("properties").isObject()) {
        logger.debug("Getting properties for object");
        schema.properties = new LinkedHashMap<String, JsonSchema>();
        el = input.get("properties");
        Iterator<Map.Entry<String, JsonNode>> it = el.fields();
        while (it.hasNext()) {
          Map.Entry<String, JsonNode> e = it.next();
          logger.debug(String.format("Adding property %s: %s", e.getKey(), e.getValue()));
          schema.properties.put(e.getKey(), buildSchema(e.getValue()));
        }
      } else
        throw new JsonSchemaTypeException("Expecting properties for object");
      if (input.hasNonNull("maxProperties"))
        schema.maxProperties = input.get("maxProperties").asInt();
      if (input.hasNonNull("minProperties"))
        schema.minProperties = input.get("minProperties").asInt();
    }
    return schema;
  }

  private JsonSchemaString buildStringSchema(JsonNode input, JsonSchemaString schema) {
    logger.debug(String.format("Building string schema for node: %s", input.toString()));
    if (input.hasNonNull("maxLength"))
      schema.maxLength = input.get("maxLength").asInt();
    if (input.hasNonNull("minLength"))
      schema.minLength = input.get("minLength").asInt();
    if (input.hasNonNull("pattern"))
      schema.pattern = input.get("pattern").asText();
    if (input.hasNonNull("default"))
      schema._default = input.get("default").asText();
    return schema;
  }


  private JsonSchemaType lookupType(String type) throws JsonSchemaTypeException {
    switch (type.toLowerCase()) {
      case "array": return JsonSchemaType.ARRAY;
      case "number": return JsonSchemaType.NUMBER;
      case "null": return JsonSchemaType.NULL;
      case "integer": return JsonSchemaType.INTEGER;
      case "object": return JsonSchemaType.OBJECT;
      case "string": return JsonSchemaType.STRING;
      case "boolean": return JsonSchemaType.BOOLEAN;
      default: throw new JsonSchemaTypeException(String.format("Invalid JSON schema _type: %s", type));
    }
  }

}
