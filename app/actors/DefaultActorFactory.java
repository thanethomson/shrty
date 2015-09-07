package actors;

import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import scala.concurrent.duration.Duration;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import play.Logger;
import repos.AuthRepo;
import repos.LinkRepo;

/**
 * Our default internal actor management factory.
 */
@Singleton
public class DefaultActorFactory implements ActorFactory {
  
  private static final Logger.ALogger logger = Logger.of(DefaultActorFactory.class);
  private final ActorRef hitCounterActor;
  private final ActorRef sessionManagerActor;

  @Inject
  public DefaultActorFactory(ActorSystem system, LinkRepo linkRepo, AuthRepo authRepo) {
    logger.debug("Initialising default actor factory and hit update counter actor...");
    hitCounterActor = system.actorOf(HitCounterActor.props(linkRepo));
    system.scheduler().schedule(
        Duration.create(HitCounterActor.UPDATE_INTERVAL, TimeUnit.SECONDS),
        Duration.create(HitCounterActor.UPDATE_INTERVAL, TimeUnit.SECONDS),
        hitCounterActor,
        "Tick",
        system.dispatcher(),
        null
        );
    
    logger.debug("Initialising session manager actor...");
    sessionManagerActor = system.actorOf(SessionManagerActor.props(authRepo));
    system.scheduler().schedule(
        Duration.create(SessionManagerActor.UPDATE_INTERVAL, TimeUnit.SECONDS),
        Duration.create(SessionManagerActor.UPDATE_INTERVAL, TimeUnit.SECONDS),
        sessionManagerActor,
        "Tick",
        system.dispatcher(),
        null
        );
  }

  @Override
  public ActorRef getHitCounterActor() {
    return hitCounterActor;
  }

  @Override
  public ActorRef getSessionManagerActor() {
    return sessionManagerActor;
  }

}
