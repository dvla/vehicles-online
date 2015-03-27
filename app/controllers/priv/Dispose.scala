package controllers.priv

import com.google.inject.Inject
import controllers.routes
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import webserviceclients.dispose.DisposeService

class Dispose @Inject()(webService: DisposeService, dateService: DateService)
                       (implicit clientSideSessionFactory: ClientSideSessionFactory,
                        config: Config) extends controllers.Dispose(webService, dateService) {

  protected override val formTarget = controllers.routes.Dispose.submit()
  protected override val backLink = controllers.routes.VehicleLookup.present()
  protected override val vehicleDetailsMissing = Redirect(routes.VehicleLookup.present())
  protected override val onVehicleAlreadyDisposed = Redirect(routes.VehicleLookup.present())
  protected override val onTraderDetailsMissing = Redirect(routes.SetUpTradeDetails.present())
  protected override val microserviceErrorCall = routes.MicroServiceError.present()
  protected override val onMicroserviceError = Redirect(microserviceErrorCall)
  protected override val onDisposeFailure = routes.DisposeFailure.present()
  protected override val onDuplicateDispose = routes.DuplicateDisposalError.present()
  protected override val onDisposeSuccess = routes.DisposeSuccess.present()
}
