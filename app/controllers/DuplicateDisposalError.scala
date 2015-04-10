package controllers

import javax.inject.Inject
import models.DisposeCacheKeys
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import utils.helpers.Config

class DuplicateDisposalError @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                         config: Config) extends Controller {

  protected val tryAgainLink = controllers.routes.VehicleLookup.present()
  protected val exitLink = controllers.routes.BeforeYouStart.present()

  def present = Action { implicit request =>
    Ok(views.html.disposal_of_vehicle.duplicate_disposal_error(tryAgainLink, exitLink))
      .discardingCookies(DisposeCacheKeys)
  }
}