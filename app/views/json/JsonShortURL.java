package views.json;

import models.ShortURL;
import models.json.JsonObject;
import utils.DateTimeConstants;

/**
 * The JSON representation of a short URL.
 */
public class JsonShortURL extends JsonObject {
  
  public Long id = null;
  public String title = null;
  public String shortCode = null;
  public String url = null;
  public Long hitCount = null;
  public String created = null;
  public JsonUser createdBy = null;

  public JsonShortURL() {}
  
  public JsonShortURL(ShortURL shortUrl) {
    this.id = shortUrl.getId();
    this.title = shortUrl.getTitle();
    this.shortCode = shortUrl.getShortCode();
    this.url = shortUrl.getUrl();
    this.hitCount = shortUrl.getHitCount();
    this.created = (shortUrl.getCreated() != null) ? DateTimeConstants.DATETIME_FORMATTER.format(shortUrl.getCreated()) : null;
    this.createdBy = (shortUrl.getCreatedBy() != null) ? new JsonUser(shortUrl.getCreatedBy()) : null;
  }

}
