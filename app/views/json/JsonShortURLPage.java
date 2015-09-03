package views.json;

import java.util.List;
import java.util.stream.Collectors;

import models.ShortURL;
import models.json.JsonObject;

public class JsonShortURLPage extends JsonObject {
  
  public Integer page = null;
  public Integer pageSize = null;
  public Long total = null;
  public Long totalUnique = null;
  public String sortBy = null;
  public String sortDir = null;
  public List<JsonShortURL> urls = null;

  public JsonShortURLPage() {}
  
  public JsonShortURLPage(int page, int pageSize, long total, long totalUnique, String sortBy, String sortDir, List<ShortURL> urls) {
    this.page = page;
    this.pageSize = pageSize;
    this.total = total;
    this.totalUnique = totalUnique;
    this.sortBy = sortBy;
    this.sortDir = sortDir;
    this.urls = urls.stream().map((url) -> new JsonShortURL(url)).collect(Collectors.toList());
  }

}
