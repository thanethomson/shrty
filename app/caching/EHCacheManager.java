package caching;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import models.Session;
import models.ShortURL;
import play.Logger;
import play.cache.CacheApi;
import play.cache.NamedCache;
import security.SecurityConstants;

/**
 * The default cache manager that makes use of EHCache (which comes with the
 * Play Framework).
 * 
 * @todo Look into a better way of handling multiple copies of this class
 * floating around than making it a singleton with synchronized functions.
 */
@Singleton
public class EHCacheManager implements CacheManager {
  
  private static final Logger.ALogger logger = Logger.of(EHCacheManager.class);
  private final CacheApi sessionCache, urlCache;

  @Inject
  public EHCacheManager(@NamedCache("session-cache") CacheApi sessionCache,
      @NamedCache("url-cache") CacheApi urlCache) {
    this.sessionCache = sessionCache;
    this.urlCache = urlCache;
  }

  @Override
  public synchronized Session storeSession(Session session) {
    sessionCache.set(session.getKey(), session, SecurityConstants.DEFAULT_SESSION_EXPIRY);
    logger.debug(String.format("Session saved in cache: %s", session.getKey()));
    return session;
  }

  @Override
  public synchronized Session findSession(String key) {
    return sessionCache.getOrElse(key, () -> null);
  }

  @Override
  public synchronized void removeSession(Session session) {
    Session cached = findSession(session.getKey());
    
    if (cached != null) {
      sessionCache.remove(cached.getKey());
      logger.debug(String.format("Removed session from cache: %s", session.getKey()));
    }
  }

  @Override
  public synchronized ShortURL storeUrl(ShortURL url) {
    urlCache.set(url.getShortCode(), url);
    logger.debug(String.format("Short URL saved in cache: %s", url.toString()));
    return url;
  }

  @Override
  public synchronized ShortURL findUrl(String code) {
    return urlCache.getOrElse(code, () -> null);
  }

  @Override
  public synchronized void removeUrl(ShortURL url) {
    ShortURL cached = findUrl(url.getShortCode());
    
    if (cached != null) {
      urlCache.remove(cached.getShortCode());
      logger.debug(String.format("Removed short URL from cache: %s", url.getShortCode()));
    }
  }

}
