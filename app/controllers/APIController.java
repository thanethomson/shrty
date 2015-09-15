package controllers;

import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import be.objectify.deadbolt.java.actions.SubjectNotPresent;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import exceptions.AlreadyExistsException;
import exceptions.DoesNotExistException;
import exceptions.InvalidPasswordException;
import forms.SignupForm;
import models.ShortURL;
import models.User;
import models.json.JsonAddShortUrl;
import models.json.JsonLogin;
import models.json.JsonSignup;
import models.Session;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import repos.AuthRepo;
import repos.LinkRepo;
import views.json.JsonError;
import views.json.JsonGenericMessage;
import views.json.JsonLoginSuccess;
import views.json.JsonShortURL;
import views.json.JsonShortURLPage;
import views.json.JsonUser;

/**
 * For handling incoming API requests.
 */
public class APIController extends BaseController {
  
  private static final Logger.ALogger logger = Logger.of(APIController.class);
  
  @Inject
  public APIController(AuthRepo authRepo, LinkRepo linkRepo) {
    super(authRepo, linkRepo);
  }
  
  /**
   * Facilitates the signing up of a user.
   * @return
   */
  @BodyParser.Of(BodyParser.Json.class)
  public Result userSignup() {
    Form<SignupForm> signupForm = Form.form(SignupForm.class);
    SignupForm signup;
    
    // bind the form to the JSON input
    signupForm = signupForm.bind(request().body().asJson());
    
    if (signupForm.hasErrors() || signupForm.hasGlobalErrors()) {
      // report back on the errors
      if (signupForm.error("firstName") != null)
        return badRequest(Json.toJson(new JsonError("Missing first name in request")));
      if (signupForm.error("lastName") != null)
        return badRequest(Json.toJson(new JsonError("Missing last name in request")));
      if (signupForm.error("email") != null)
        return badRequest(Json.toJson(new JsonError("Missing or invalid email address in request")));
      if (signupForm.error("password") != null)
        return badRequest(Json.toJson(new JsonError("Missing password in request")));
      
      // generic bad request error
      return badRequest(Json.toJson(new JsonError("Invalid request")));
    }
    
    signup = signupForm.get();
    logger.debug(String.format("Received signup request: %s", signup.toString()));
    
    // try to create the user
    User user;
    
    try {
      user = authRepo.createUser(signup.getFirstName(), signup.getLastName(),
          signup.getEmail(), signup.getPassword());
    } catch (NoSuchAlgorithmException e) {
      logger.error("Unable to create new user", e);
      return internalServerError(Json.toJson(new JsonError("Internal server error")));
    } catch (AlreadyExistsException e) {
      logger.error("Unable to create new user", e);
      return badRequest(Json.toJson(new JsonError(String.format("User with e-mail address %s already exists in database", signup.getEmail()))));
    }
    
    logger.debug(String.format("Successfully created user with ID: %d", user.getId()));
    
    // all's well
    return ok(Json.toJson(new JsonUser(user)));
  }
  
  
  /**
   * Tries to retrieve the user with the specified ID.
   * @param id
   * @return
   */
  @SubjectPresent
  public Result getUserById(int id) {
    User user = authRepo.findUserById(id);
    
    if (user == null) {
      return notFound(Json.toJson(new JsonError(String.format("Could not find user with ID %d", id))));
    } else {
      return ok(Json.toJson(new JsonUser(user)));
    }
  }
  
  
  /**
   * Tries to retrieve the user with the specified e-mail address.
   * @param email
   * @return
   */
  @SubjectPresent
  public Result getUserByEmail(String email) {
    User user = authRepo.findUserByEmail(email);
    
    if (user == null) {
      return notFound(Json.toJson(new JsonError(String.format("Could not find user with e-mail %s", email))));
    } else {
      return ok(Json.toJson(new JsonUser(user)));
    }
  }
  
  
  /**
   * Allows one to request a paged listing of all of the short URLs via the API.
   * @param query A case-insensitive search string by which to filter URLs.
   * @param page The page number to retrieve (starting from 0).
   * @param pageSize The number of records to retrieve per page.
   * @param sortBy The column by which to sort the records.
   * @param sortDir The direction in which to sort the records (asc|desc).
   * @return
   */
  @SubjectPresent
  public Result getShortUrls(String query, Integer page, Integer pageSize, String sortBy, String sortDir) {
    // make sure the sortBy field is valid
    if (!sortBy.matches("^(title|shortCode|url|hitCount|created|createdBy)$")) {
      logger.error(String.format("Logging invalid incoming sortBy value: %s", sortBy));
      return badRequest(Json.toJson(new JsonError("Invalid field name for sortBy")));
    }
    
    if (!sortDir.matches("^(asc|desc)$")) {
      logger.error(String.format("Logging invalid incoming sortDir value: %s", sortDir));
      return badRequest(Json.toJson(new JsonError("Invalid sort direction for sortDir")));
    }
    
    logger.debug(String.format("Getting short URLs, page %d, page size %d, sorted by %s %s", page, pageSize, sortBy, sortDir));
    
    // try to get the relevant page of links
    return ok(Json.toJson(new JsonShortURLPage(
        page,
        pageSize,
        linkRepo.getLinkCount(query, page, pageSize, sortBy, sortDir),
        sortBy,
        sortDir,
        linkRepo.getLinks(query, page, pageSize, sortBy, sortDir))));
  }
  
  
  /**
   * Allows one to fetch details about a specific short URL via the API by way of its short code.
   * @param code
   * @return
   */
  @SubjectPresent
  public Result getShortUrl(String code) {
    // try to find the short URL
    ShortURL shortUrl = linkRepo.findLinkByShortCode(code);
    
    if (shortUrl == null)
      return notFound(Json.toJson(new JsonError(String.format("Cannot find short URL with code: %s", code))));
    
    // otherwise return the short URL's details
    return ok(Json.toJson(new JsonShortURL(shortUrl)));
  }
  
  
  /**
   * Allows one to add short URLs via the JSON API.
   */
  @SubjectPresent
  @BodyParser.Of(BodyParser.Json.class)
  public Result addShortUrl() {
    JsonAddShortUrl addShortUrl = Json.fromJson(request().body().asJson(), JsonAddShortUrl.class);
    Session session = getSession();
    
    if (session == null) {
      logger.error("Unable to find session in context data");
      return internalServerError(Json.toJson(new JsonError("Internal server error")));
    }
    
    // check the short URL's properties
    if (addShortUrl.title == null || addShortUrl.title.length() == 0) {
      return badRequest(Json.toJson(new JsonError("Missing title in request")));
    } else if (addShortUrl.url == null || addShortUrl.url.length() == 0) {
      return badRequest(Json.toJson(new JsonError("Missing URL in request")));
    }
    
    ShortURL shortUrl = linkRepo.createLink(addShortUrl.title, addShortUrl.url, addShortUrl.shortCode, session.getUser());
    logger.debug(String.format("Created short URL: %s", shortUrl.toString()));
    return ok(Json.toJson(new JsonShortURL(shortUrl)));
  }
  
  
  /**
   * Allows one to delete short URLs with the specified code.
   * @param code The short code for which to search.
   */
  @SubjectPresent
  public Result deleteShortUrls(String code) {
    logger.debug(String.format("Attempting to hide short URLs with code: %s", code));
    
    int deleted = linkRepo.makeSecondary(code);
    logger.debug(String.format("Hid %d short URL(s)", deleted));
    
    return ok(Json.toJson(new JsonGenericMessage(String.format("Deleted %d link(s)", deleted))));
  }
  
  
  /**
   * Attempts to perform an API-based login, where the session ID will be returned in the JSON payload.
   */
  @SubjectNotPresent
  @BodyParser.Of(BodyParser.Json.class)
  public Result login() {
    JsonLogin req = Json.fromJson(request().body().asJson(), JsonLogin.class);
    
    if (req.email == null || req.email.length() == 0) {
      return badRequest(Json.toJson(new JsonError("Missing e-mail address in request")));
    } else if (req.password == null || req.password.length() == 0) {
      return badRequest(Json.toJson(new JsonError("Missing password in request")));
    }
    
    logger.debug(String.format("Incoming login request for user: %s", req.email));
    
    // attempt to log the user in
    Session session;
    
    try {
      session = authRepo.login(req.email, req.password, session());
    } catch (NoSuchAlgorithmException e) {
      logger.error("Cannot get hash algorithm", e);
      return internalServerError(Json.toJson(new JsonError("Internal server error")));
    } catch (DoesNotExistException e) {
      logger.error(String.format("E-mail address %s does not exist in database", req.email), e);
      return notFound(Json.toJson(new JsonError(String.format("Cannot find user with e-mail address: %s", req.email))));
    } catch (InvalidPasswordException e) {
      return badRequest(Json.toJson(new JsonError("Invalid password")));
    }
    
    logger.debug(String.format("Successfully logged user %s in; session key: %s", req.email, session.getKey()));
    return ok(Json.toJson(new JsonLoginSuccess(session)));
  }
  
  
  /**
   * Attempts to perform an API-based logging out of the user for whom the session is relevant.
   */
  @SubjectPresent
  public Result logout() {
    Session session = getSession();
    
    if (session != null) {
      logger.debug(String.format("Attempting to end session: %s", session.getKey()));
      authRepo.logout(session, session());
    }
    
    return ok(Json.toJson(new JsonGenericMessage("OK")));
  }
  
}
