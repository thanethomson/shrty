package modules;

import com.google.inject.AbstractModule;

import actors.ActorFactory;
import actors.DefaultActorFactory;
import caching.CacheManager;
import caching.EHCacheManager;
import play.Logger;

/**
 * Dependency injection module for configuring our setup for testing purposes.
 */
public class TestModule extends AbstractModule {
  
  private static final Logger.ALogger logger = Logger.of(TestModule.class);

  @Override
  protected void configure() {
    logger.debug("Initialising application with TestModule DI");
    // set up the actor factory
    bind(ActorFactory.class).to(DefaultActorFactory.class).asEagerSingleton();
    // set up the caching system
    bind(CacheManager.class).to(EHCacheManager.class).asEagerSingleton();
  }

}
