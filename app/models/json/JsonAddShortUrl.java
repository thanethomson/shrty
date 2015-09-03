package models.json;

/**
 * Received via the API to add a short URL.
 */
public class JsonAddShortUrl extends JsonObject {
  
  public String title = null;
  public String url = null;
  public String shortCode = null;

  public JsonAddShortUrl() {}
  
  public JsonAddShortUrl(String title, String url, String shortCode) {
    this.title = title;
    this.url = url;
    this.shortCode = shortCode;
  }

}
