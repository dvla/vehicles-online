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
    logMessage(request.cookies.trackingId(), Error, s"Displaying generic error page for digest - $exceptionDigest")
    Ok(views.html.disposal_of_vehicle.error(formTarget(exceptionDigest)))
  }

  def submit(exceptionDigest: String) = Action { implicit request =>
    val msg = "Submit called on Error controller - now removing full set of cookies and redirecting to Start page"
    logMessage(request.cookies.trackingId(), Error, msg)
    CookieHelper.discardAllCookies(routes.BeforeYouStart.present())
  }
}