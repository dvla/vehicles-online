package controllers.priv

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class DisposeFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                 config: Config) extends controllers.DisposeFailure {

  protected override val sellNewVehicleCall = routes.VehicleLookup.present()
  protected override val exitCall = routes.SetUpTradeDetails.present()
  protected override val onMissingCookies = Redirect(routes.SetUpTradeDetails.present())
}
