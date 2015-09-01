package modules;

import com.google.inject.AbstractModule;
import security.CacheSessionManager;
import security.SessionManager;

/**
 * Sets up dependency injection bindings for local development mode.
 */
public class DevModule extends AbstractModule {

    @Override
    protected void configure() {
        // we want to use the cache-based session manager
        bind(SessionManager.class).to(CacheSessionManager.class);
    }

}
