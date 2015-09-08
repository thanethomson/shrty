package controllers;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.raml.model.Raml;
import org.raml.parser.visitor.RamlDocumentBuilder;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import play.*;
import play.mvc.*;
import utils.jsonschema.JsonSchemaBuilder;
import utils.jsonschema.JsonSchemaObject;
import views.html.api.*;

/**
 * Automatically generates an HTML interface from the RAML API documentation.
 */
public class APIDocController extends BaseController {

  public APIDocController() {
    super(null, null);
  }

  private static final Logger.ALogger logger = Logger.of(APIDocController.class);

  /**
   * Converts the internal RAML docs for the API to a human-readable format.
   */
  @SubjectPresent
  public Result showHumanReadable() {
    String content;
    Raml raml;

    try {
      content = IOUtils.toString(Play.application()
          .classloader()
          .getResourceAsStream("api.raml"), "UTF-8");
    } catch (IOException e) {
      return internalServerError(String.format("Unable to fetch api.raml file: %s", e.getMessage()));
    }

    raml = new RamlDocumentBuilder().build(content, "api.raml");
    Map<String, JsonSchemaObject> schemas = new LinkedHashMap<String, JsonSchemaObject>();
    try {
      for (Map<String, String> maps: raml.getSchemas()) {
        for (Map.Entry<String, String> e: maps.entrySet()) {
          schemas.put(e.getKey(), (JsonSchemaObject)(new JsonSchemaBuilder(e.getValue()).getSchema()));
        }
      }
    } catch (Exception e) {
      logger.error("Unable to parse schema", e);
      return internalServerError(e.getMessage());
    }
    return ok(apiDoc.render(getSession(), request(), raml, schemas));
  }

  /**
   * Outputs the RAML docs directly.
   */
  @SubjectPresent
  public Result showRaml() {
    response().setContentType("application/raml");
    try {
      return ok(IOUtils.toString(Play.application()
          .classloader()
          .getResourceAsStream("api.raml"), "UTF-8"));
    } catch (IOException e) {
      return internalServerError(String.format("Unable to fetch api.raml file: %s", e.getMessage()));
    }
  }

}
