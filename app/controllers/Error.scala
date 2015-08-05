package controllers

import com.google.inject.Inject
import play.api.mvc.{Call, Action}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import utils.helpers.{Config, CookieHelper}

class Error @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                        config: Config) extends BusinessController {

  protected def formTarget(exceptionDigest: String): Call = controllers.routes.Error.submit(exceptionDigest)

  def present(exceptionDigest: String) = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Error, s"Displaying generic error page - " +
      s"$exceptionDigest")
    Ok(views.html.disposal_of_vehicle.error(formTarget(exceptionDigest)))
  }

  // TODO is there a submit button that calls this? If it is unused then delete.
  def submit(exceptionDigest: String) = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Error, s"Submit called - now removing full set of cookies and " +
      s"redirecting to Start page")
    CookieHelper.discardAllCookies
  }


}
