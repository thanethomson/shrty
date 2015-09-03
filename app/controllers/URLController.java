package controllers;

import com.google.inject.Inject;

import models.ShortURL;
import play.Logger;
import play.mvc.*;
import play.twirl.api.Html;
import repos.LinkRepo;

/**
 * Handles incoming requests to short code URLs and redirects the caller to the relevant page.
 */
public class URLController extends Controller {

  private static final Logger.ALogger logger = Logger.of(URLController.class);
  private final LinkRepo linkRepo;
  
  @Inject
  public URLController(LinkRepo linkRepo) {
    this.linkRepo = linkRepo;
  }
  
  /**
   * The primary routing function for Shrty.
   * @param code
   * @return
   */
  public Result route(String code) {
    // try to look up the relevant short URL
    ShortURL shortUrl = linkRepo.cachedLinkLookup(code);
    
    if (shortUrl != null) {
      logger.debug(String.format("Incoming short code %s, routing to %s", code, shortUrl.getUrl()));
      return redirect(shortUrl.getUrl());
    } else {
      logger.debug(String.format("Cannot find link for short code: %s", code));
    }
    
    // sorry, can't find the short code you're looking for
    return notFound((Html)views.html.notFound.render(request()));
  }

}
