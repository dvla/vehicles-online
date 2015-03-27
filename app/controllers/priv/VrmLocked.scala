package controllers.priv

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class VrmLocked @Inject()()(implicit protected override val clientSideSessionFactory: ClientSideSessionFactory,
                            config: Config) extends controllers.VrmLocked {

  protected override val tryAnotherTarget = controllers.routes.VrmLocked.tryAnother()
  protected override val exitTarget = controllers.routes.VrmLocked.exit()
  protected override val bruteForceCookieMissing = Redirect(controllers.routes.VehicleLookup.present())
  protected override val lookupAnotherVehicle = Redirect(controllers.routes.VehicleLookup.present())
  protected override val onExit = Redirect(config.startUrl)
}
