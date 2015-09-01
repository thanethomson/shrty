package controllers;

import play.mvc.*;
import play.twirl.api.Html;

/**
 * The administrative interface for Shrty.
 */
public class AdminController extends Controller {

    public Result index() {
        return ok((Html)views.html.login.render(request()));
    }

    public Result about() {
        return ok((Html)views.html.about.render(request(), false));
    }

}
