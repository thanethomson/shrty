package actors;

import java.util.Date;

import akka.actor.Props;
import akka.actor.UntypedActor;
import play.Logger;
import repos.LinkRepo;

/**
 * An actor which periodically updates the link hit counts in the database from the cached
 * hit count information.
 */
public class HitCounterActor extends UntypedActor {
  
  private static final Logger.ALogger logger = Logger.of(HitCounterActor.class);
  /** The number of seconds to wait between updates to the hit counters. */
  public static final Integer UPDATE_INTERVAL = 10;
  
  public static Props props(LinkRepo linkRepo) {
    return Props.create(HitCounterActor.class, linkRepo);
  }
  
  private final LinkRepo linkRepo;
  private Date lastUpdate = new Date();
  
  public HitCounterActor(LinkRepo linkRepo) {
    this.linkRepo = linkRepo;
  }

  @Override
  public void onReceive(Object message) throws Exception {
    if (message instanceof String && ((String)message).equals("Tick")) {
      logger.debug("Hit counter actor tick");
      
      // we include this check just in case it takes us longer than the update interval to
      // actually perform the hit counter update - so we don't flood the actor with a
      // backlog of hit counter updates until the server falls over :-)
      Date now = new Date();
      long updateDiff = (now.getTime() - lastUpdate.getTime()) / 1000;
      
      if (updateDiff >= UPDATE_INTERVAL) {
        logger.debug("Updating hit information for links...");
        linkRepo.updateHitCountsFromCache();
        logger.debug("Updated hit count information from cache.");
        
        lastUpdate = now;
      }
    } else {
      unhandled(message);
    }
  }

}
