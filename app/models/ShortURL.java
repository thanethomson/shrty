package models;

import com.avaje.ebean.Model;
import play.data.validation.*;
import utils.DateTimeConstants;

import javax.persistence.*;
import java.util.Date;

/**
 * A single shortened URL and its properties. Note that it is possible to have multiple short URLs with the same
 * short code - as is the case when a short URL's long URL is updated. In these cases, the most recently created
 * short URL will be selected for redirection.
 */
@Entity
@Table(name="short_urls")
public class ShortURL extends Model {

    /** The database ID for this short URL. */
    @Id
    private Long id;

    /** A short, descriptive title for this short URL. */
    @Constraints.Required
    private String title;

    /** The short code for this URL. */
    @Constraints.Required
    private String shortCode;

    /** The fully expanded URL to which we must redirect. */
    @Constraints.Required
    private String url;

    /** The number of hits this specific short URL has received. */
    private Long hitCount;

    /** The date/time at which this specific short URL was created. */
    private Date created;

    /** The user who created this short URL. */
    @ManyToOne
    private User createdBy;

    
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder();
      String newline = System.getProperty("line.separator");
      
      buf.append("{"+newline);
      buf.append(String.format("  id        = %d%s", id, newline));
      buf.append(String.format("  title     = %s%s", title, newline));
      buf.append(String.format("  shortCode = %s%s", shortCode, newline));
      buf.append(String.format("  url       = %s%s", url, newline));
      buf.append(String.format("  hitCount  = %d%s", hitCount, newline));
      buf.append(String.format("  created   = %s%s", (created != null) ? DateTimeConstants.DATETIME_FORMATTER.format(created) : "null", newline));
      buf.append(String.format("  createdBy = %s%s", (createdBy != null) ? createdBy.toString() : "null", newline));
      buf.append("}"+newline);
      
      return buf.toString();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getHitCount() {
        return hitCount;
    }

    public void setHitCount(Long hitCount) {
        this.hitCount = hitCount;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
