package models;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import com.avaje.ebean.Model;
import play.data.validation.*;
import utils.DateTimeConstants;
import views.json.JsonUser;

import javax.persistence.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Database representation of a user and their profile details.
 */
@Entity
@Table(name="users")
public class User extends Model implements Subject {

    /** This user's database ID. */
    @Id
    private Long id;

    /** This user's first name. */
    @Constraints.Required
    private String firstName;

    /** This user's last name. */
    @Constraints.Required
    private String lastName;

    /** This user's e-mail address. */
    @Constraints.Required
    @Constraints.Email
    private String email;

    /** A hash of the user's password. */
    @Constraints.Required
    private String passwordHash;

    /** The date/time at which this entry was first created. */
    private Date created;
    
    
    public User() {
      super();
    }
    
    /**
     * Constructor to build up properties from the given JSON user object.
     * @param jsonUser
     */
    public User(JsonUser jsonUser) {
      super();
      setId(jsonUser.id);
      setFirstName(jsonUser.firstName);
      setLastName(jsonUser.lastName);
      setEmail(jsonUser.email);
      if (jsonUser.created != null) {
        try {
          setCreated(DateTimeConstants.DATETIME_FORMATTER.parse(jsonUser.created));
        } catch (ParseException e) {
          setCreated(null);
        }
      }
    }

    
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder();
      String newline = System.getProperty("line.separator");
      
      buf.append("{"+newline);
      buf.append(String.format("  id           = %d%s", id, newline));
      buf.append(String.format("  firstName    = %s%s", firstName, newline));
      buf.append(String.format("  lastName     = %s%s", lastName, newline));
      buf.append(String.format("  email        = %s%s", email, newline));
      buf.append(String.format("  passwordHash = %s%s", passwordHash, newline));
      buf.append(String.format("  created      = %s%s", (created != null) ? DateTimeConstants.DATETIME_FORMATTER.format(created) : "null", newline));
      buf.append("}"+newline);
      
      return buf.toString();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }


    @Override
    public List<? extends Role> getRoles() {
        // roles are irrelevant here
        return new ArrayList<Role>();
    }

    @Override
    public List<? extends Permission> getPermissions() {
        // permissions are also irrelevant here at present
        return new ArrayList<Permission>();
    }

    @Override
    public String getIdentifier() {
        return getEmail();
    }
}
