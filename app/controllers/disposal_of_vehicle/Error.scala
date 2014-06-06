package controllers.disposal_of_vehicle

import play.api.mvc._
import play.api.Logger
import com.google.inject.Inject
import utils.helpers.CryptoHelper

final class Error @Inject()() extends Controller {
  def present = Action { implicit request =>
    Logger.debug("Error - Displaying generic error page")
    Ok(views.html.disposal_of_vehicle.error())
  }

  def submit = Action.async { implicit request =>
    Logger.debug("Error submit called - now removing full set of cookies and redirecting to Start page")
    CryptoHelper.discardAllCookies
  }

}