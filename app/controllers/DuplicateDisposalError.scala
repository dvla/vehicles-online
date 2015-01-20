package controllers

import javax.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import utils.helpers.Config
import models.DisposeCacheKeys

final class DuplicateDisposalError @Inject()()
                                   (implicit clientSideSessionFactory: ClientSideSessionFactory, config: Config)
  extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.disposal_of_vehicle.duplicate_disposal_error()).discardingCookies(DisposeCacheKeys)
  }
}