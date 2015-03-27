package controllers.priv

import com.google.inject.Inject
import controllers.{routes, SurveyUrl}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupService
import utils.helpers.Config

class VehicleLookup @Inject()(implicit bruteForceService: BruteForcePreventionService,
                              vehicleAndKeeperLookupService: VehicleAndKeeperLookupService,
                              surveyUrl: SurveyUrl,
                              dateService: DateService,
                              clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends controllers.VehicleLookup {

  protected override val submitTarget = controllers.routes.VehicleLookup.submit()
  protected override val exitTarget = controllers.routes.VehicleLookup.exit()
  protected override val onVrmLocked = Redirect(controllers.routes.VrmLocked.present())
  protected override val onMicroServiceError = Redirect(controllers.routes.MicroServiceError.present())
  protected override val onVehicleLookupFailure = Redirect(controllers.routes.VehicleLookupFailure.present())
  protected override val missingTradeDetails = Redirect(controllers.routes.SetUpTradeDetails.present())
  protected override val enterAddressManually = Redirect(controllers.routes.EnterAddressManually.present())
  protected override val businessChooseYourAddress = Redirect(controllers.routes.BusinessChooseYourAddress.present())
  protected override val suppressedV5C = Redirect(controllers.routes.SuppressedV5C.present())
  protected override val duplicateDisposalError = Redirect(controllers.routes.DuplicateDisposalError.present())
  protected override val dispose = Redirect(controllers.routes.Dispose.present())
  protected override val onExit = Redirect(config.endUrl)
}
