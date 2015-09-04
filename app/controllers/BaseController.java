package controllers;

import models.Session;
import play.mvc.Controller;
import play.mvc.Http;
import repos.AuthRepo;
import repos.LinkRepo;

/**
 * Provides some common helper functionality for our controllers.
 */
public abstract class BaseController extends Controller {
  
  protected final AuthRepo authRepo;
  protected final LinkRepo linkRepo;
  
  public BaseController(AuthRepo authRepo, LinkRepo linkRepo) {
    this.authRepo = authRepo;
    this.linkRepo = linkRepo;
  }
  
  /**
   * Helper function to look up the session from the incoming session/request info.
   * @return A Session object on success, or null on failure.
   */
  protected Session getSession() {
    // if we have a session from the context (e.g. from the Deadbolt handler)
    if (Http.Context.current().args.containsKey("session"))
      return (Session)Http.Context.current().args.get("session");
    
    // otherwise try to look it up from the session info
    return authRepo.getSessionFromRequest(session(), request());
  }

}
