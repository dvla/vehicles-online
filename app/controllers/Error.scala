package controllers

import com.google.inject.Inject
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import utils.helpers.{Config, CookieHelper}

class Error @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

  def present(exceptionDigest: String) = Action { implicit request =>
    Logger.debug(s"Error - Displaying generic error page with tracking id: ${request.cookies.trackingId()}")
    Ok(views.html.disposal_of_vehicle.error(exceptionDigest))
  }

  // TODO is there a submit button that calls this? If it is unused then delete.
  def submit(exceptionDigest: String) = Action.async { implicit request =>
    Logger.debug(s"Error submit called - now removing full set of cookies and " +
      s"redirecting to Start page with tracking id: ${request.cookies.trackingId()}")
    CookieHelper.discardAllCookies
  }
}