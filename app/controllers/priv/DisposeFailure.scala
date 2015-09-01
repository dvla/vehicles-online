package controllers.priv

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class DisposeFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                 config: Config) extends controllers.DisposeFailure with PrivateKeeperController {

  override protected val sellNewVehicleCall = routes.VehicleLookup.present()
  override protected val exitCall = routes.SetUpTradeDetails.present()
  override protected val onMissingCookies = Redirect(routes.SetUpTradeDetails.present())

  override protected val DisposeFormTransactionIdCacheKey =
    models.PrivateDisposeFormModel.DisposeFormTransactionIdCacheKey
}