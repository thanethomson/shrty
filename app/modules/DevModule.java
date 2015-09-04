package modules;

import com.google.inject.AbstractModule;

import actors.ActorFactory;
import actors.DefaultActorFactory;
import play.Logger;

/**
 * Sets up dependency injection bindings for local development mode.
 */
public class DevModule extends AbstractModule {

  private static final Logger.ALogger logger = Logger.of(DevModule.class);

  @Override
  protected void configure() {
    logger.debug("Initialising application with DevModule DI");
    // we want the default actor factory, but as an eager singleton - create the necessary actor(s) on startup
    bind(ActorFactory.class).to(DefaultActorFactory.class).asEagerSingleton();
  }

}
