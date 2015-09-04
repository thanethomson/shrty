package actors;

import akka.actor.ActorRef;

/**
 * The interface for the actor(s) of which the Shrty application makes use.
 */
public interface ActorFactory {
  
  /** Allows us to get an ActorRef to the hit counter actor. */
  public ActorRef getHitCounterActor();
  
  /** Allows us to get an ActorRef to the actor responsible for managing session data. */
  public ActorRef getSessionManagerActor();

}
