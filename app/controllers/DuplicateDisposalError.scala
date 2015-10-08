package controllers

import javax.inject.Inject
import play.api.mvc.Action
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichResult, RichCookies}
import utils.helpers.Config

class DuplicateDisposalError @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                         config: Config) extends BusinessController {

  protected val tryAgainLink = controllers.routes.VehicleLookup.present()
  protected val exitLink = controllers.routes.BeforeYouStart.present()

  def present = Action { implicit request =>
    val trackingId = request.cookies.trackingId()
    logMessage(trackingId, Info, "Presenting duplicate disposal view")
    Ok(views.html.disposal_of_vehicle.duplicate_disposal_error(tryAgainLink, exitLink, trackingId))
      .discardingCookies(DisposeCacheKeys)
  }
}