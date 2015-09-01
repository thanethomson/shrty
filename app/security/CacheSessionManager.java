package security;

import com.google.inject.Inject;
import play.cache.CacheApi;

/**
 * The primary session manager for Shrty - the EHCache-based one.
 */
public class CacheSessionManager implements SessionManager {

    private final CacheApi cache;

    @Inject
    public CacheSessionManager(CacheApi cache) {
        this.cache = cache;
    }

    @Override
    public Session find(String id) {
        return cache.getOrElse(String.format("session.%s", id), null);
    }

    @Override
    public Session save(Session session) {
        // update its expiry date/time
        session.touch();
        cache.set(String.format("session.%s", session.getKey()), session, Session.DEFAULT_SESSION_EXPIRY);
        return session;
    }

}
