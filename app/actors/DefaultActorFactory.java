package actors;

import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import scala.concurrent.duration.Duration;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import play.Logger;
import repos.LinkRepo;

@Singleton
public class DefaultActorFactory implements ActorFactory {
  
  private static final Logger.ALogger logger = Logger.of(DefaultActorFactory.class);
  private final ActorRef hitCounterActor;

  @Inject
  public DefaultActorFactory(ActorSystem system, LinkRepo linkRepo) {
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
  }

  @Override
  public ActorRef getHitCounterActor() {
    return hitCounterActor;
  }

  @Override
  public ActorRef getSessionManagerActor() {
    // TODO Auto-generated method stub
    return null;
  }

}
