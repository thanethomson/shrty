package security;

import models.User;

import java.util.Calendar;
import java.util.Date;

/**
 * Sessions are stored and managed in-memory in the caching system.
 */
public class Session {

    /** The default number of seconds after which the session expires after no activity. */
    public final static Integer DEFAULT_SESSION_EXPIRY = 60*60;

    /** The session key. */
    private String key = null;
    /** The user for whom this session is relevant. */
    private User user = null;
    /** The date/time at which this session was initiated. */
    private Date started = null;
    /** The date/time at which this session expires. */
    private Date expires = null;

    public Session() {}

    public Session(String key, User user) {
        setKey(key);
        setUser(user);
        setStarted(new Date());
        touch();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }


    /**
     * Call this function to update the expiry date/time for this session.
     */
    public void touch() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, DEFAULT_SESSION_EXPIRY);

        // set the new session expiry date/time
        setExpires(cal.getTime());
    }
}
