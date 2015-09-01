package security;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import com.google.inject.Inject;
import play.Logger;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import views.json.JsonError;

import java.util.Optional;

/**
 * The default security handler for Shrty.
 */
public class DefaultDeadboltHandler extends AbstractDeadboltHandler {

    private static final Logger.ALogger logger = Logger.of(DefaultDeadboltHandler.class);
    public final static String SESSIONKEY_HEADER = "SESSIONID";
    private final SessionManager sessionManager;

    @Inject
    public DefaultDeadboltHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public F.Promise<Optional<Result>> beforeAuthCheck(Http.Context context) {
        return F.Promise.promise(Optional::empty);
    }

    @Override
    public F.Promise<Optional<Subject>> getSubject(final Http.Context context) {
        // if we have a session ID in the incoming request
        if (context.request().hasHeader(SESSIONKEY_HEADER)) {
            return F.Promise.promise(() -> {
                String sessionId = context.request().getHeader(SESSIONKEY_HEADER);
                logger.debug(String.format("Incoming request with session ID: %s", sessionId));

                // try to look up the session ID in the cache
                Session session = sessionManager.find(sessionId);

                // if there is no such session, access denied
                if (session == null) {
                    logger.debug(String.format("No session in cache with ID: %s", sessionId));
                    return Optional.empty();
                }

                // if there is a session
                logger.debug(String.format("Found session in cache for user: %s", session.getUser().toString()));

                // update the session
                sessionManager.save(session);

                // return the user for the session
                return Optional.ofNullable(session.getUser());
            });
        } else {
            logger.debug("Request has no session key header - no subject present");
            // no access
            return F.Promise.promise(Optional::empty);
        }
    }

    @Override
    public F.Promise<Result> onAuthFailure(final Http.Context context, final String content) {
        // check the content type
        if (context.request().hasHeader("Content-Type")) {
            String contentType = context.request().getHeader("Content-Type");

            switch (contentType) {
                case "application/json":
                    return F.Promise.promise(() -> forbidden(new JsonError("You do not have permission to access this resource").toJsonNode()));

                default:
                    return F.Promise.promise(() -> forbidden());
            }
        } else {
            return F.Promise.promise(() -> forbidden());
        }
    }

}
