package security;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import com.google.inject.Inject;
import play.Logger;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import play.twirl.api.Html;
import repos.AuthRepo;
import views.json.JsonError;
import models.Session;

import java.util.Optional;

/**
 * The default security handler for Shrty.
 */
public class DefaultDeadboltHandler extends AbstractDeadboltHandler {

  private static final Logger.ALogger logger = Logger.of(DefaultDeadboltHandler.class);
  private final AuthRepo authRepo;

  @Inject
  public DefaultDeadboltHandler(AuthRepo authRepo) {
    this.authRepo = authRepo;
  }

  @Override
  public F.Promise<Optional<Result>> beforeAuthCheck(Http.Context context) {
    return F.Promise.promise(Optional::empty);
  }

  @Override
  public F.Promise<Optional<Subject>> getSubject(final Http.Context context) {
    // try to get the session's details from the incoming session/request data
    final Session session = authRepo.getSessionFromRequest(context.session(), context.request());

    // if we have a session ID in the incoming request
    if (session != null) {
      logger.debug(String.format("Found session with ID: %s", session.getKey()));
      return F.Promise.promise(() -> {
        // if there is a session
        logger.debug(String.format("Found session in cache for user: %s", session.getUser().toString()));
        
        // touch the session, updating its expiry details
        Session touched = authRepo.touchSession(session);
        // keep track of the session in the context
        context.args.put("session", touched);

        // return the user for the session
        return Optional.ofNullable(touched.getUser());
      });
    } else {
      logger.debug("No session or expired session for incoming request");
      // no subject
      return F.Promise.promise(Optional::empty);
    }
  }

  @Override
  public F.Promise<Result> onAuthFailure(final Http.Context context, final String content) {
    // check the content type
    if (context.request().accepts("application/json") ||
        (context.request().hasHeader("Content-Type") &&
            context.request().getHeader("Content-Type").equals("application/json"))) {
      return F.Promise.promise(() -> forbidden(new JsonError("You do not have permission to access this resource").toJsonNode()));
    } else {
      // otherwise return HTML
      return F.Promise.promise(() -> forbidden(views.html.forbidden.render(
          (Session)context.args.getOrDefault("session", null),
          context.request()
          )));
    }
  }

}
