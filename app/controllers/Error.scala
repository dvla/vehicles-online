package controllers

import com.google.inject.Inject
import play.api.Logger
import play.api.mvc.{Call, Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import common.LogFormats.logMessage
import utils.helpers.{Config, CookieHelper}

class Error @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                        config: Config) extends Controller {

  protected def formTarget(exceptionDigest: String): Call = controllers.routes.Error.submit(exceptionDigest)

  def present(exceptionDigest: String) = Action { implicit request =>
    Logger.info(s"Error - Displaying generic error page - " +
      s"${exceptionDigest} - " +
      s"trackingId: ${request.cookies.trackingId()}")
    Ok(views.html.disposal_of_vehicle.error(formTarget(exceptionDigest)))
  }

  // TODO is there a submit button that calls this? If it is unused then delete.
  def submit(exceptionDigest: String) = Action.async { implicit request =>
    Logger.debug(s"Error submit called - now removing full set of cookies and " +
      s"redirecting to Start page - trackingId: ${request.cookies.trackingId()}")
    CookieHelper.discardAllCookies
  }
}
