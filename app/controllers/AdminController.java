package controllers;

import java.security.NoSuchAlgorithmException;

import com.google.inject.Inject;

import be.objectify.deadbolt.java.actions.SubjectNotPresent;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import exceptions.DoesNotExistException;
import exceptions.InvalidPasswordException;
import forms.LoginForm;
import play.Logger;
import play.data.Form;
import play.mvc.*;
import play.twirl.api.Html;
import repos.AuthRepo;
import security.Session;
import security.SessionManager;

/**
 * The administrative interface for Shrty.
 */
public class AdminController extends Controller {
  
  private static final Logger.ALogger logger = Logger.of(AdminController.class);
  private final AuthRepo authRepo;
  private final SessionManager sessionManager;
  
  @Inject
  public AdminController(AuthRepo authRepo, SessionManager sessionManager) {
    this.authRepo = authRepo;
    this.sessionManager = sessionManager;
  }

  /**
   * The web application home page.
   * @return
   */
  public Result index() {
    logger.debug("Index page loading...");
    Session session = sessionManager.get(session(), request());
    // if there's no session, redirect the user to the login page
    if (session == null) {
      logger.debug("No session object found in incoming request");
      return redirect(controllers.routes.AdminController.showLogin());
    } else {
      logger.debug(String.format("Found session in context args: %s", session.getKey()));
      // otherwise, redirect them to the administration page
      return redirect(controllers.routes.AdminController.admin());
    }
  }
  
  /**
   * The administrative page, where logged in users can administer their short URLs.
   * @return
   */
  @SubjectPresent
  public Result admin() {
    return ok((Html)views.html.admin.render(
        (Session)Http.Context.current().args.getOrDefault("session", null),
        request()
        ));
  }
  
  
  /**
   * Login page.
   * @return
   */
  @SubjectNotPresent
  public Result showLogin() {
    return ok((Html)views.html.login.render(
        (Session)Http.Context.current().args.getOrDefault("session", null),
        request(),
        null));
  }
  
  
  /**
   * Handles incoming login requests.
   * @return
   */
  @SubjectNotPresent
  public Result doLogin() {
    Form<LoginForm> form = Form.form(LoginForm.class).bindFromRequest(request());
    LoginForm loginForm = form.get();
    Session session = null;
    
    // if we have an e-mail address and password
    if (!form.hasErrors() && !form.hasGlobalErrors()) {
      // try to log the user in
      try {
        session = authRepo.login(loginForm.getEmail(), loginForm.getPassword(), session());
      } catch (NoSuchAlgorithmException e) {
        return internalServerError((Html)views.html.internalServerError.render(null, request()));
      } catch (DoesNotExistException e) {
        form.reject("email", "Unrecognised e-mail address");
      } catch (InvalidPasswordException e) {
        form.reject("password", "Invalid password");
      }
    }
    
    // check if there are any form errors
    if (form.hasErrors() || form.hasGlobalErrors()) {
      logger.debug("Incoming form has errors - displaying login page again");
      form.fill(new LoginForm(loginForm.getEmail(), null));
      return ok((Html)views.html.login.render(null, request(), form));
    }
    
    logger.debug(String.format("Created new session: %s", session.getKey()));
    
    // otherwise, all's good - go home
    return redirect(controllers.routes.AdminController.index());
  }
  
  /**
   * Logs the current user out and redirects them to the home page.
   * @return
   */
  @SubjectPresent
  public Result doLogout() {
    Session session = (Session)Http.Context.current().args.getOrDefault("session", null);
    if (session != null) {
      authRepo.logout(session, session());
    }
    return redirect(controllers.routes.AdminController.index());
  }

  /**
   * About page, to show more info about the app.
   * @return
   */
  public Result about() {
    return ok((Html)views.html.about.render(
        sessionManager.get(session(), request()),
        request()));
  }

}
