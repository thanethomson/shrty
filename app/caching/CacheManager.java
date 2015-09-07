package caching;

import models.Session;
import models.ShortURL;

/**
 * The interface for our cache manager - allows us to swap out different
 * caching systems as we need them.
 */
public interface CacheManager {
  
  /**
   * Stores the specified session in the cache.
   * @param session The session to be stored in cache.
   * @return The cached session object.
   */
  public Session storeSession(Session session);
  
  /**
   * Attempts to find the session with the specified key in the cache.
   * @param key The session key for which to search.
   * @return A Session object on success, or null if not found.
   */
  public Session findSession(String key);
  
  /**
   * Removes the specified session from the cache, if it exists.
   * @param session The session to be removed from the cache.
   */
  public void removeSession(Session session);
  
  
  /**
   * Stores the specified short URL in the cache.
   * @param url The short URL object to be stored.
   * @return The cached short URL.
   */
  public ShortURL storeUrl(ShortURL url);
  
  /**
   * Attempts to find the short URL with the specified short code in the cache.
   * @param code The short code for the URL for which to search.
   * @return A ShortURL object on success, or null if not found.
   */
  public ShortURL findUrl(String code);
  
  /**
   * Attempts to remove the specified short URL from the cache, if it exists.
   * @param url The short URL to be removed from the cache.
   */
  public void removeUrl(ShortURL url);

}
