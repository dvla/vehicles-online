package controllers

import com.google.inject.Inject
import play.api.mvc.{Call, Action}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import common.utils.helpers.CookieHelper
import utils.helpers.Config

class Error @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                        config: Config) extends BusinessController {

  protected def formTarget(exceptionDigest: String): Call = controllers.routes.Error.submit(exceptionDigest)

  def present(exceptionDigest: String) = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Error, "Displaying generic error page - " +
      exceptionDigest)
    Ok(views.html.disposal_of_vehicle.error(formTarget(exceptionDigest)))
  }

  // TODO is there a submit button that calls this? If it is unused then delete.
  def submit(exceptionDigest: String) = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Error, "Submit called - now removing full set of cookies and " +
      "redirecting to Start page")
    CookieHelper.discardAllCookies(routes.BeforeYouStart.present)
  }
}