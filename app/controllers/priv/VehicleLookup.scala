package controllers.priv

import com.google.inject.Inject
import controllers.SurveyUrl
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
                              config: Config)
  extends controllers.VehicleLookup with PrivateKeeperController {

  protected override val submitTarget = routes.VehicleLookup.submit()
  protected override val backTarget = routes.VehicleLookup.back()
  protected override val exitTarget = routes.VehicleLookup.exit()
  protected override val onVrmLocked = Redirect(routes.VrmLocked.present())
  protected override val onMicroServiceError = Redirect(routes.MicroServiceError.present())
  protected override val onVehicleLookupFailure = Redirect(routes.VehicleLookupFailure.present())
  protected override val missingTradeDetails = Redirect(routes.SetUpTradeDetails.present())
  protected override val enterAddressManually = Redirect(routes.EnterAddressManually.present())
  protected override val businessChooseYourAddress = Redirect(routes.BusinessChooseYourAddress.present())
  protected override val suppressedV5C = Redirect(routes.SuppressedV5C.present())
  protected override val duplicateDisposalError = Redirect(routes.DuplicateDisposalError.present())
  protected override val dispose = Redirect(routes.Dispose.present())
  protected override val onExit = Redirect(controllers.routes.BeforeYouStart.present())
}
