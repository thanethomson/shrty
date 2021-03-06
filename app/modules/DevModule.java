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
 * Sets up dependency injection bindings for local development mode.
 */
public class DevModule extends AbstractModule {

  private static final Logger.ALogger logger = Logger.of(DevModule.class);
  private final Configuration configuration;
  
  public DevModule(Environment environment, Configuration configuration) {
    this.configuration = configuration;
    
    logger.debug(String.format("Database URL: %s", configuration.getString("db.default.url")));
    logger.debug(String.format("Application secret: %s", configuration.getString("play.crypto.secret")));
  }

  @Override
  protected void configure() {
    logger.debug("Initialising application with DevModule DI");
    // we want the default actor factory, but as an eager singleton - create the necessary actor(s) on startup
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
