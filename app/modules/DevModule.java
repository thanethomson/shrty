package modules;

import com.google.inject.AbstractModule;

import actors.ActorFactory;
import actors.DefaultActorFactory;
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
        // we want the default actor factory, but as an eager singleton - create the necessary actor(s) on startup
        bind(ActorFactory.class).to(DefaultActorFactory.class).asEagerSingleton();
    }

}
