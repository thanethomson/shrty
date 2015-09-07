package integration;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import actors.HitCounterActor;
import models.Session;
import models.ShortURL;
import models.User;
import models.json.JsonAddShortUrl;
import models.json.JsonLogin;
import models.json.JsonSignup;
import play.Application;
import play.Environment;
import play.Logger;
import play.Mode;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.test.WithServer;
import security.SecurityConstants;
import views.json.JsonGenericMessage;
import views.json.JsonLoginSuccess;
import views.json.JsonShortURL;
import views.json.JsonUser;

public class APITests extends WithServer {
  
  private static final Logger.ALogger logger = Logger.of(APITests.class);
  private final int TIMEOUT = 5000;
  private User manderson, gmichaels;
  
  @Override
  protected int providePort() {
    return 3333;
  }
  
  @Override
  protected Application provideApplication() {
    return new GuiceApplicationBuilder()
        .in(Environment.simple())
        .in(Mode.TEST)
        .build();
  }
  
  /**
   * Helper function to return the absolute URL for the given relative path.
   * @param relPath The relative API path, with leading slash.
   * @return A string containing the absolute URL.
   */
  protected String urlFor(String relPath) {
    return String.format("http://localhost:%d%s", testServer.port(), relPath);
  }
  
  protected void cleanDatabase() {
    int deleted = 0;
    
    deleted += Ebean.delete(Ebean.find(ShortURL.class).findList());
    deleted += Ebean.delete(Ebean.find(Session.class).findList());
    deleted += Ebean.delete(Ebean.find(User.class).findList());
    
    logger.debug(String.format("Deleted %s database entries in cleanup", deleted));
  }
  
  @Before
  public void setUp() {
    logger.debug("Starting APITests test");
  }
  
  @After
  public void tearDown() {
    logger.debug("Cleaning up models after API test...");
    cleanDatabase();
    logger.debug("Done.");
  }

  @Test
  public void test() {
    // user/auth tests
    createUsers();
    authTests();
    
    // short URL-related tests
    shortUrlTests();
  }
  
  protected void createUsers() {
    WSResponse response;
    JsonUser jsonUser;
    
    // create a user
    response = postRequest("/api/user", Json.toJson(new JsonSignup("Michael", "Anderson", "manderson@gmail.com", "12345")));
    assertEquals(200, response.getStatus());
    jsonUser = parseJson(JsonUser.class, response.getBody());
    manderson = Ebean.find(User.class).where().idEq(jsonUser.id).findUnique();
    logger.debug(String.format("Created user: %s", manderson));
    
    // try to create another user with the same e-mail address
    response = postRequest("/api/user", Json.toJson(new JsonSignup("Michelle", "Anderson", "manderson@gmail.com", "12345")));
    assertEquals(400, response.getStatus());
    
    // create a second user
    response = postRequest("/api/user", Json.toJson(new JsonSignup("Gary", "Michaels", "gmichaels@gmail.com", "12345")));
    assertEquals(200, response.getStatus());
    jsonUser = parseJson(JsonUser.class, response.getBody());
    gmichaels = Ebean.find(User.class).where().idEq(jsonUser.id).findUnique();
    logger.debug(String.format("Created user: %s", gmichaels));
  }
  
  protected void authTests() {
    WSResponse response;
    JsonUser jsonUser;
    String sessionId;
    
    // try to log in as one of the users
    response = postRequest("/api/login", Json.toJson(new JsonLogin("manderson@gmail.com", "12345")));
    assertEquals(200, response.getStatus());
    
    JsonLoginSuccess jsonLogin = parseJson(JsonLoginSuccess.class, response.getBody());
    logger.debug(String.format("Logged in with session ID: %s", jsonLogin.sessionId));
    sessionId = jsonLogin.sessionId;
    
    // try to get details on user himself...
    // ... first without session key
    response = getRequest(String.format("/api/user/%s", manderson.getEmail()));
    assertEquals(403, response.getStatus());
    
    // ... then with session key
    response = getRequest(String.format("/api/user/%s", manderson.getEmail()), jsonLogin.sessionId);
    assertEquals(200, response.getStatus());
    jsonUser = parseJson(JsonUser.class, response.getBody());
    compareUsers(jsonUser, manderson);
    
    // try to log in again as the same user
    response = postRequest("/api/login", Json.toJson(new JsonLogin("manderson@gmail.com", "12345")));
    assertEquals(200, response.getStatus());
    
    // make sure that the session ID is the same (it wasn't closed previously)
    jsonLogin = parseJson(JsonLoginSuccess.class, response.getBody());
    assertEquals(sessionId, jsonLogin.sessionId);
    
    // now log out
    response = postRequest("/api/logout", Json.toJson(new JsonGenericMessage("Logout")), jsonLogin.sessionId);
    assertEquals(200, response.getStatus());
    
    // now we shouldn't be able to perform any more authenticated requests with this particular session key
    response = getRequest(String.format("/api/user/%s", manderson.getEmail()), jsonLogin.sessionId);
    assertEquals(403, response.getStatus());
  }
  
  protected void compareUsers(JsonUser jsonUser, User user) {
    assertEquals(user.getId(), jsonUser.id);
    assertEquals(user.getEmail(), jsonUser.email);
    assertEquals(user.getFirstName(), jsonUser.firstName);
    assertEquals(user.getLastName(), jsonUser.lastName);
  }
  
  
  protected void shortUrlTests() {
    WSResponse response;
    String sessionId;
    
    // first log someone in
    sessionId = doAPILogin("manderson@gmail.com", "12345", 200);
    
    // now try to create some URLs, but without an auth key
    response = postRequest("/api/shorturl", Json.toJson(new JsonAddShortUrl("First test URL", urlFor("/login"), null)));
    assertEquals(403, response.getStatus());
    
    // now create one, but this time with an auth key
    response = postRequest("/api/shorturl", Json.toJson(new JsonAddShortUrl("First test URL", urlFor("/login"), null)), sessionId);
    assertEquals(200, response.getStatus());
    JsonShortURL shortUrl = parseJson(JsonShortURL.class, response.getBody());
    assertEquals("First test URL", shortUrl.title);
    assertEquals(urlFor("/login"), shortUrl.url);
    assertNotNull(shortUrl.shortCode);
    assertEquals(manderson.getId(), shortUrl.createdBy.id);
    
    // get the short URL's details via the GET API
    response = getRequest(String.format("/api/shorturl/%s", shortUrl.shortCode), sessionId);
    assertEquals(200, response.getStatus());
    JsonShortURL shortUrl2 = parseJson(JsonShortURL.class, response.getBody());
    compareShortUrls(shortUrl, shortUrl2);
    
    // now try to actually facilitate a redirect
    response = getRequest(String.format("/%s", shortUrl.shortCode));
    assertEquals(200, response.getStatus());
    assertEquals(urlFor("/login"), response.getUri().toString());
    
    logger.debug(String.format("Waiting %d seconds for hit counter actor to update hit count...", HitCounterActor.UPDATE_INTERVAL+1));
    // wait a bit to have the hit counter synchronise with the database
    try {
      Thread.sleep((HitCounterActor.UPDATE_INTERVAL+1) * 1000);
    } catch (InterruptedException e) {
      logger.error("Thread sleep interrupted", e);
      fail(e.getMessage());
    }
    
    // now look up some info about the short URL
    response = getRequest(String.format("/api/shorturl/%s", shortUrl.shortCode), sessionId);
    assertEquals(200, response.getStatus());
    shortUrl = parseJson(JsonShortURL.class, response.getBody());
    // make sure that the hit count has been updated
    assertEquals(Long.valueOf(1L), shortUrl.hitCount);
    
    // try to create a custom short URL
    response = postRequest("/api/shorturl", Json.toJson(new JsonAddShortUrl("Custom short code test", urlFor("/login"), "MyCode")), sessionId);
    assertEquals(200, response.getStatus());
    shortUrl = parseJson(JsonShortURL.class, response.getBody());
    assertEquals("Custom short code test", shortUrl.title);
    assertEquals(urlFor("/login"), shortUrl.url);
    assertEquals("MyCode", shortUrl.shortCode);
    assertEquals(manderson.getId(), shortUrl.createdBy.id);
    
    // now try to actually facilitate a redirect
    response = getRequest("/MyCode");
    assertEquals(200, response.getStatus());
    assertEquals(urlFor("/login"), response.getUri().toString());
    
    // log in as another user
    String sessionId2 = doAPILogin("gmichaels@gmail.com", "12345", 200);
    
    // now try to overwrite the custom short URL
    response = postRequest("/api/shorturl", Json.toJson(new JsonAddShortUrl("Overwritten short code", urlFor("/about"), "MyCode")), sessionId2);
    assertEquals(200, response.getStatus());
    shortUrl = parseJson(JsonShortURL.class, response.getBody());
    assertEquals("Overwritten short code", shortUrl.title);
    assertEquals(urlFor("/about"), shortUrl.url);
    assertEquals("MyCode", shortUrl.shortCode);
    assertEquals(gmichaels.getId(), shortUrl.createdBy.id);
    
    // now try to actually facilitate a redirect
    response = getRequest("/MyCode");
    assertEquals(200, response.getStatus());
    assertEquals(urlFor("/about"), response.getUri().toString());
    
    // get the short URL info - should be equal to the new version now
    response = getRequest("/api/shorturl/MyCode", sessionId);
    assertEquals(200, response.getStatus());
    shortUrl2 = parseJson(JsonShortURL.class, response.getBody());
    compareShortUrls(shortUrl, shortUrl2);
    
    // make sure that there are 2 entries in the database for the "MyCode" short code
    assertEquals(2, Ebean.find(ShortURL.class).where().eq("shortCode", shortUrl.shortCode).findRowCount());
    
    // now delete the custom short URL
    response = deleteRequest("/api/shorturl/MyCode", sessionId);
    assertEquals(200, response.getStatus());
    
    // now make sure that there are 0 primary entries in the database for the "MyCode" short code
    assertEquals(0, Ebean.find(ShortURL.class).where().eq("primary", true).eq("shortCode", shortUrl.shortCode).findRowCount());
  }
  
  
  protected String doAPILogin(String email, String password, int expectedStatus) {
    WSResponse response = postRequest("/api/login", Json.toJson(new JsonLogin(email, password)));
    assertEquals(expectedStatus, response.getStatus());
    
    if (expectedStatus == 200) {
      JsonLoginSuccess jsonLogin = parseJson(JsonLoginSuccess.class, response.getBody());
      return jsonLogin.sessionId;
    }
    
    return null;
  }
  
  
  protected void compareShortUrls(JsonShortURL url1, JsonShortURL url2) {
    assertEquals(url1.id, url2.id);
    assertEquals(url1.shortCode, url2.shortCode);
    assertEquals(url1.title, url2.title);
    assertEquals(url1.created, url2.created);
    assertEquals(url1.createdBy.id, url2.createdBy.id);
    assertEquals(url1.url, url2.url);
  }
  
  
  protected WSResponse getRequest(String relPath) {
    logger.debug(String.format("GET (unauthenticated): %s", relPath));
    return WS.url(urlFor(relPath)).get().get(TIMEOUT);
  }
  
  protected WSResponse getRequest(String relPath, String sessionKey) {
    logger.debug(String.format("GET (authenticated): %s", relPath));
    return WS.url(urlFor(relPath))
        .setHeader(SecurityConstants.SESSIONKEY_HEADER, sessionKey)
        .get()
        .get(TIMEOUT);
  }
  
  protected WSResponse postRequest(String relPath, JsonNode json) {
    logger.debug(String.format("POST (unauthenticated): %s", relPath));
    return WS.url(urlFor(relPath)).post(json).get(TIMEOUT);
  }
  
  protected WSResponse postRequest(String relPath, JsonNode json, String sessionKey) {
    logger.debug(String.format("POST (authenticated): %s", relPath));
    return WS.url(urlFor(relPath))
        .setHeader(SecurityConstants.SESSIONKEY_HEADER, sessionKey)
        .post(json)
        .get(TIMEOUT);
  }
  
  protected WSResponse deleteRequest(String relPath, String sessionKey) {
    logger.debug(String.format("DELETE (authenticated): %s", relPath));
    return WS.url(urlFor(relPath))
        .setHeader(SecurityConstants.SESSIONKEY_HEADER, sessionKey)
        .delete()
        .get(TIMEOUT);
  }
  
  protected <T> T parseJson(Class<T> model, String src) {
    T result;
    
    try {
      result = Json.fromJson(new ObjectMapper().readTree(src), model);
      return result;
    } catch (Exception e) {
      logger.error(String.format("Unable to parse JSON: %s", src), e);
      fail(e.getMessage());
      return null;
    }
  }

}
