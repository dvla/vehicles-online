package controllers

import com.google.inject.Inject
import play.api.mvc.Action
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class SoapEndpointError @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config) extends BusinessController {

  protected val disposeTarget = controllers.routes.Dispose.present()
  protected val exitTarget = controllers.routes.BeforeYouStart.present()

  def present = Action { implicit request =>
    Ok(views.html.disposal_of_vehicle.soap_endpoint_error(disposeTarget, exitTarget))
  }
}
