package repos;

import java.security.SecureRandom;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hashids.Hashids;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.QueryIterator;
import com.google.inject.Inject;

import models.ShortURL;
import models.User;
import play.Logger;
import play.cache.CacheApi;
import play.cache.NamedCache;
import security.SecurityConstants;

/**
 * Repository for managing short links.
 */
public class LinkRepo {
  
  private static final Logger.ALogger logger = Logger.of(LinkRepo.class);
  private final CacheApi cacheApi;
  
  @Inject
  public LinkRepo(@NamedCache("url-cache") CacheApi cacheApi) {
    this.cacheApi = cacheApi;
  }
  
  /**
   * Attempts to generate a random, unique short code.
   * @return
   */
  public String generateUniqueShortCode() {
    Hashids hashids = new Hashids(SecurityConstants.SHORTCODE_HASH_SALT,
        SecurityConstants.SHORTCODE_LENGTH,
        SecurityConstants.SHORTCODE_CHARSET);
    SecureRandom random = new SecureRandom();
    random.setSeed(new Date().getTime());
    String result = null, code;
    
    // encode random integers until we find a valid code
    while (result == null) {
      code = hashids.encode(random.nextInt(10000000));
      // check if the code exists in the database
      if (findLinkByShortCode(code) == null) {
        result = code;
      }
    }
    
    return result;
  }
  
  /**
   * Attempts to create a database entry for the specified short code/URL.
   * @param title A short, descriptive title for the new entry.
   * @param url The fully expanded URL to which to redirect.
   * @param shortCode The short code to use (if null, a unique short code will automatically be generated).
   * @param user The user creating the link.
   * @return
   */
  public ShortURL createLink(String title, String url, String shortCode, User user) {
    String code = (shortCode != null && shortCode.length() > 0) ? shortCode : generateUniqueShortCode();
    
    // first make all other links for this short code secondary
    makeSecondary(code);
    
    ShortURL result = new ShortURL();
    
    result.setTitle(title);
    result.setUrl(url);
    result.setShortCode(code);
    result.setHitCount(0L);
    result.setCreated(new Date());
    result.setCreatedBy(user);
    // this is now the primary link
    result.setPrimary(true);
    
    // save the short URL to the database
    Ebean.save(result);
    
    // cache it
    cacheLink(result);
    
    logger.debug(String.format("Created new short URL: %s", result.toString()));    
    return result;
  }
  
  
  /**
   * Makes all of the links with the specified short code secondary.
   * @param shortCode
   * @return The number of links updated.
   * 
   * @todo Figure out a way to optimise this.
   */
  public int makeSecondary(String shortCode) {
    final int[] count = {0};
    
    Ebean.find(ShortURL.class)
      .where()
        .eq("shortCode", shortCode)
      .findEach((ShortURL url) -> {
        url.setPrimary(false);
        Ebean.save(url);
        count[0]++;
      });
    
    logger.debug(String.format("%d link(s) made secondary", count[0]));
    return count[0];
  }
  
  
  /**
   * Attempts to delete all of the links with the given short code.
   * @param shortCode
   * @return
   */
  public int deleteLinks(String shortCode) {
    logger.debug(String.format("Attempting to delete all links with short code: %s", shortCode));
    // make sure we remove any entries from the cache
    ShortURL cached = getCachedLink(shortCode);
    if (cached != null) {
      logger.debug("Also removing link from cache...");
      uncacheLink(cached);
    }
    return Ebean.delete(Ebean.find(ShortURL.class)
      .where()
        .eq("shortCode", shortCode)
      .findList());
  }
  
  
  /**
   * Internal helper function to build up an ExpressionList object for a short URL query.
   * @param query
   * @param page
   * @param pageSize
   * @param sortBy
   * @param sortDir
   * @return
   */
  protected ExpressionList<ShortURL> buildLinkFetchExpr(String query, int page, int pageSize, String sortBy, String sortDir) {
    ExpressionList<ShortURL> e = Ebean.find(ShortURL.class).where().eq("primary", true);
    
    // if we have a query, build up the filtering criteria
    if (query != null && query.length() > 0) {
      e = e.or(
          Expr.ilike("title", String.format("%%%s%%", query)),
          Expr.or(
             Expr.ilike("shortCode", String.format("%%%s%%", query)),
             Expr.ilike("url", String.format("%%%s%%", query)))
          );
    }
    
    return e;
  }
  
  
  /**
   * Allows for paged retrieval of links.
   * @param query A case-insensitive search query by which to filter URLs.
   * @param page The page number to retrieve (starting from 0).
   * @param pageSize The number of links to retrieve per page.
   * @param sortBy The column by which to sort links.
   * @param sortDir The sort direction (asc|desc).
   * @return
   */
  public List<ShortURL> getLinks(String query, int page, int pageSize, String sortBy, String sortDir) {
    logger.debug(String.format("Attempting to fetch page %d of links, page size %d, sorted by %s %s", page, pageSize, sortBy, sortDir));
    return buildLinkFetchExpr(query, page, pageSize, sortBy, sortDir)
        .orderBy(String.format("%s %s", sortBy, sortDir))
        .findPagedList(page, pageSize)
        .getList();
  }
  
  
  /**
   * Attempts to look up all of the short URL entries in the database, updating their hit
   * counts from their corresponding entries in the cache.
   */
  public void updateHitCountsFromCache() {
    long updateCount = 0;
    
    logger.debug("Attempting to update hit counts from cache...");
    
    // start a transaction for this update
    Ebean.beginTransaction();
    
    try {
      QueryIterator<ShortURL> iterator = Ebean.find(ShortURL.class)
          .orderBy("shortCode asc, created desc")
          .findIterate();
      Set<String> seenCodes = new HashSet<String>();
      
      try {
        while (iterator.hasNext()) {
          ShortURL url = iterator.next();
          // see if we have a cached version of this URL
          ShortURL cachedUrl = getCachedLink(url.getShortCode());
          
          // selectively update the entries
          if (cachedUrl != null && !seenCodes.contains(cachedUrl.getShortCode()) && cachedUrl.getHitCount() > url.getHitCount()) {
            // update the entry's hit count
            url.setHitCount(cachedUrl.getHitCount());
            // update it
            Ebean.save(url);
            
            // keep track of which short codes we've already seen
            seenCodes.add(cachedUrl.getShortCode());
            updateCount++;
          }
        }
      } finally {
        iterator.close();
      }
      
      // commit the changes
      logger.debug("Committing hit count update transaction...");
      Ebean.commitTransaction();
      
    } finally {
      Ebean.endTransaction();
    }
    
    logger.debug(String.format("Updated %d entries' hit counts", updateCount));
  }
  
  
  /**
   * Retrieves a list of all of the unique/distinct short codes in the database.
   * @return
   */
  public List<ShortURL> getUniqueShortCodes() {
    return Ebean.find(ShortURL.class)
        .where()
          .eq("primary", true)
        .findList();
  }
  
  
  /**
   * Retrieves the total number of links in the database.
   * @return
   */
  public long getLinkCount() {
    return Ebean.find(ShortURL.class)
        .findRowCount();
  }
  
  
  /**
   * Helper function to get the number of primary links for the given query.
   * @param query
   * @param page
   * @param pageSize
   * @param sortBy
   * @param sortDir
   * @return
   */
  public long getLinkCount(String query, int page, int pageSize, String sortBy, String sortDir) {
    return buildLinkFetchExpr(query, page, pageSize, sortBy, sortDir).findRowCount();
  }
  
  
  /**
   * Retrieves the total number of unique links in the database.
   */
  public long getUniqueLinkCount() {
    return Ebean.find(ShortURL.class)
        .where()
          .eq("primary", true)
        .findRowCount();
  }
  
  
  /**
   * Attempts to find the latest link by way of its short code.
   * @param shortCode The short code for which to search.
   * @return A ShortURL object on success, or null if no such entry exists.
   */
  public ShortURL findLinkByShortCode(String shortCode) {
    return Ebean.find(ShortURL.class)
        .where()
          .eq("primary", true)
          .eq("shortCode", shortCode)
          .orderBy("created desc")
        .setMaxRows(1)
        .findUnique();
  }
  
  /**
   * Puts the given short URL into the cache.
   * @param shortUrl
   */
  public void cacheLink(ShortURL shortUrl) {
    cacheApi.set(String.format("url.%s", shortUrl.getShortCode()), shortUrl);
    logger.debug(String.format("Cached short URL entry for code: %s", shortUrl.getShortCode()));
  }
  
  /**
   * Allows one to try to retrieve a cached ShortURL object by way of its short code.
   * @param shortCode
   * @return A ShortURL object on success, or null if it could not be found.
   */
  public ShortURL getCachedLink(String shortCode) {
    return cacheApi.getOrElse(String.format("url.%s", shortCode), () -> null);
  }
  
  /**
   * Removes the given short URL from the cache.
   * @param shortUrl
   */
  public void uncacheLink(ShortURL shortUrl) {
    String path = String.format("url.%s", shortUrl.getShortCode());
    if (cacheApi.getOrElse(path, () -> null) != null) {
      cacheApi.remove(path);
      logger.debug(String.format("Removed short URL entry from cache: %s", shortUrl.getShortCode()));
    }
  }
  
  /**
   * Performs a cached link lookup: if the URL with the given short code cannot be found in the cache,
   * this attempts to look it up from the database.
   * @param shortCode
   * @return A ShortURL object on success, or null on failure.
   */
  public ShortURL cachedLinkLookup(String shortCode) {
    ShortURL result = cacheApi.getOrElse(String.format("url.%s", shortCode), () -> findLinkByShortCode(shortCode));
    
    // if we found the relevant link
    if (result != null) {
      // update its hit counter in memory
      result.setHitCount(result.getHitCount()+1);
      // update it in the cache
      cacheLink(result);
      logger.debug(String.format("Updated hit count for %s to %d", result.getShortCode(), result.getHitCount()));
    }
    
    return result;
  }

}
