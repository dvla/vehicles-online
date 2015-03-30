package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class UprnNotFound @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                     config: Config) extends Controller {

  protected val enterAddressManuallyTarget = controllers.routes.EnterAddressManually.present()
  protected val setupTradeDetailsTarget = controllers.routes.SetUpTradeDetails.present()

  def present = Action { implicit request =>
    Ok(views.html.disposal_of_vehicle.uprn_not_found(enterAddressManuallyTarget, setupTradeDetailsTarget))
  }
}