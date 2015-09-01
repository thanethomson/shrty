package modules;

import be.objectify.deadbolt.java.cache.HandlerCache;
import com.google.inject.Singleton;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;
import security.DefaultDeadboltHandlerCache;

/**
 * The default Deadbolt module for this application, setting up the handler cache.
 */
public class DefaultDeadboltModule extends Module {

    @Override
    public Seq<Binding<?>> bindings(final Environment environment, final Configuration configuration) {
        return seq(bind(HandlerCache.class).to(DefaultDeadboltHandlerCache.class).in(Singleton.class));
    }

}
