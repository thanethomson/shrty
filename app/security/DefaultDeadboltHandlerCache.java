package security;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;
import com.google.inject.Singleton;
import play.Play;

import java.util.HashMap;
import java.util.Map;

/**
 * The default Deadbolt handler cache.
 */
@Singleton
public class DefaultDeadboltHandlerCache implements HandlerCache {

    private final DeadboltHandler defaultHandler;
    private final Map<String, DeadboltHandler> handlers = new HashMap<>();

    public DefaultDeadboltHandlerCache() {
        // instantiate our handler
        defaultHandler = Play.application().injector().instanceOf(DefaultDeadboltHandler.class);
        handlers.put(HandlerKeys.DEFAULT.key, defaultHandler);
        // use the default handler for API requests for now
        handlers.put(HandlerKeys.API.key, defaultHandler);
    }

    @Override
    public DeadboltHandler apply(final String key) {
        return handlers.get(key);
    }

    @Override
    public DeadboltHandler get() {
        return defaultHandler;
    }

}
