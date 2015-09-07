package modules;

import com.google.inject.AbstractModule;

import actors.ActorFactory;
import actors.DefaultActorFactory;
import caching.CacheManager;
import caching.EHCacheManager;
import caching.RedisCacheManager;
import play.Configuration;
import play.Environment;
import play.Logger;

/**
 * Dependency injection module for configuring our setup for testing purposes.
 */
public class TestModule extends AbstractModule {
  
  private static final Logger.ALogger logger = Logger.of(TestModule.class);
  private final Configuration configuration;
  
  public TestModule(Environment environment, Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    logger.debug("Initialising application with TestModule DI");
    // set up the actor factory
    bind(ActorFactory.class).to(DefaultActorFactory.class).asEagerSingleton();
    
    // set up the caching system
    String cacheSystem = configuration.getString("shrty.cache.system", "ehcache");
    
    // we currently support EHCache and Redis with default connection options
    if (cacheSystem.equals("ehcache")) {
      logger.debug("Using EHCache as default cache back-end");
      bind(CacheManager.class).to(EHCacheManager.class).asEagerSingleton();
    } else if (cacheSystem.equals("redis")){
      logger.debug("Using Redis as default cache back-end");
      bind(CacheManager.class).to(RedisCacheManager.class).asEagerSingleton();
    }
  }

}
