package actors;

import java.util.Date;

import akka.actor.Props;
import akka.actor.UntypedActor;
import play.Logger;
import repos.AuthRepo;

/**
 * Watches the session info and marks sessions as expired if they have been
 * inactive for too long.
 */
public class SessionManagerActor extends UntypedActor {

  private static final Logger.ALogger logger = Logger.of(SessionManagerActor.class);
  /** The number of seconds between each session update check. */
  public static final Integer UPDATE_INTERVAL = 10;
  
  public static Props props(AuthRepo authRepo) {
    return Props.create(SessionManagerActor.class, authRepo);
  }
  
  private final AuthRepo authRepo;
  private Date lastUpdate = new Date();
  
  public SessionManagerActor(AuthRepo authRepo) {
    this.authRepo = authRepo;
  }

  @Override
  public void onReceive(Object message) throws Exception {
    if (message instanceof String && message.equals("Tick")) {
      Date now = new Date();
      long updateDiff = (now.getTime() - lastUpdate.getTime()) / 1000;
      
      // make sure there's at least [UPDATE_INTERVAL] seconds between each update,
      // otherwise skip this tick
      if (updateDiff >= UPDATE_INTERVAL) {
        logger.debug("Updating session information...");
        authRepo.checkExpiredSessions();
        lastUpdate = now;
      }
    }
  }

}
