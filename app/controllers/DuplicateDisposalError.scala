package controllers

import javax.inject.Inject
import models.DisposeCacheKeys
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichResult, RichCookies}
import common.LogFormats.logMessage
import utils.helpers.Config

class DuplicateDisposalError @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                         config: Config) extends BusinessController {

  protected val tryAgainLink = controllers.routes.VehicleLookup.present()
  protected val exitLink = controllers.routes.BeforeYouStart.present()

  def present = Action { implicit request =>
    Logger.info(logMessage(s"Duplicate disposal page", request.cookies.trackingId()))
    Ok(views.html.disposal_of_vehicle.duplicate_disposal_error(tryAgainLink, exitLink))
      .discardingCookies(DisposeCacheKeys)
  }
}