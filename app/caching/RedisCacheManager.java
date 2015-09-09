package caching;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import models.Session;
import models.ShortURL;
import models.json.JsonSession;
import play.Configuration;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.F;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import views.json.JsonShortURL;

/**
 * A cache system manager built on top of Redis. This uses the plain and simple
 * Jedis API for Redis interaction (i.e. not the clustered version). It also
 * checks the configuration file for the address (host/port) of our Redis
 * server, and defaults to localhost:6379 if none was configured.
 * 
 * @todo Write a clustered Redis version of this cache manager.
 */
public class RedisCacheManager implements CacheManager {
  
  private static final Logger.ALogger logger = Logger.of(RedisCacheManager.class);
  private final JedisPool jedisPool;
  private String redisHost;
  private Integer redisPort;

  @Inject
  public RedisCacheManager(ApplicationLifecycle lifecycle, Configuration config) {
    logger.debug("Starting up Redis-based cache manager");
    
    redisHost = config.getString("shrty.cache.redis.host", null);
    if (redisHost == null) {
      logger.debug("No Redis host specified in configuration file, using default host: localhost");
      redisHost = "localhost";
    } else {
      logger.debug(String.format("Using configured Redis host: %s", redisHost));
    }
    
    redisPort = config.getInt("shrty.cache.redis.port", null);
    if (redisPort == null) {
      logger.debug("No Redis port specified in configuration file, using default port: 6379");
      redisPort = 6379;
    } else {
      logger.debug(String.format("Using configured Redis port: %d", redisPort));
    }
    
    // set up the jedis pool for our multithreaded environment
    jedisPool = new JedisPool(new JedisPoolConfig(), redisHost, redisPort);
    
    // we need to destroy our pool when the application stops
    lifecycle.addStopHook(() -> {
      logger.debug("Cleaning up Redis-based cache manager");
      jedisPool.destroy();
      return F.Promise.pure(null);
    });
  }
  
  
  /**
   * Helper function to execute code with a Jedis connection sourced from the
   * Jedis connection pool.
   * @param fn The function to execute.
   * @return
   */
  protected <T> T withJedis(F.Function<Jedis, T> fn) {
    try (Jedis jedis = jedisPool.getResource()) {
      return fn.apply(jedis);
    } catch (Throwable e) {
      logger.error("Unable to execute Redis cache interaction", e);
      return null;
    }
  }
  
  /**
   * Similar to the above function, except that it doesn't return any value.
   * @param fn
   */
  protected void withJedis (F.Callback<Jedis> fn) {
    try (Jedis jedis = jedisPool.getResource()) {
      fn.invoke(jedis);
    } catch (Throwable e) {
      logger.error("Unable to execute Redis cache interaction", e);
    }
  }
  
  protected String jedisSessionKey(String key) {
    return String.format("session.%s", key);
  }
  
  protected String jedisUrlKey(String shortCode) {
    return String.format("url.%s", shortCode);
  }
  
  @Override
  public Session storeSession(Session session) {
    return withJedis((Jedis jedis) -> {
      String jk = jedisSessionKey(session.getKey());
      // convert the session to a JSON object and store its string representation
      jedis.set(jk, new JsonSession(session).toString());
      logger.debug(String.format("Stored serialised session object in Redis cache at key: %s", jk));
      return session;
    });
  }

  @Override
  public Session findSession(String key) {
    return withJedis((Jedis jedis) -> {
      String jk = jedisSessionKey(key);
      // try to look up the key
      if (jedis.exists(jk))
        return new Session(new ObjectMapper().readValue(jedis.get(jk), JsonSession.class));
      // not found
      return null;
    });
  }

  @Override
  public void removeSession(Session session) {
    withJedis((Jedis jedis) -> {
      String jk = jedisSessionKey(session.getKey());
      
      // remove the session data
      if (jedis.exists(jk)) {
        jedis.del(jk);
        logger.debug(String.format("Removed session key from Redis cache: %s", jk));
      }
    });
  }

  @Override
  public ShortURL storeUrl(ShortURL url) {
    return withJedis((Jedis jedis) -> {
      String jk = jedisUrlKey(url.getShortCode());
      jedis.set(jk, new JsonShortURL(url).toString());
      logger.debug(String.format("Stored serialised short URL object in Redis cache at key: %s", jk));
      return url;
    });
  }

  @Override
  public ShortURL findUrl(String code) {
    return withJedis((Jedis jedis) -> {
      String jk = jedisUrlKey(code);
      
      // try to look up the URL
      if (jedis.exists(jk))
        return new ShortURL(new ObjectMapper().readValue(jedis.get(jk), JsonShortURL.class));
      return null;
    });
  }

  @Override
  public void removeUrl(ShortURL url) {
    withJedis((Jedis jedis) -> {
      String jk = jedisUrlKey(url.getShortCode());
      
      if (jedis.exists(jk)) {
        jedis.del(jk);
        logger.debug(String.format("Removed short URL from Redis cache: %s", url.getShortCode()));
      }
    });
  }

}
