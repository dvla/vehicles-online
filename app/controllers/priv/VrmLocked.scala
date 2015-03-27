package controllers.priv

import com.google.inject.Inject
import controllers.routes
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class VrmLocked @Inject()()(implicit protected override val clientSideSessionFactory: ClientSideSessionFactory,
                            config: Config) extends controllers.VrmLocked {

  protected override val bruteForceCookieMissing = Redirect(routes.VehicleLookup.present())
  protected override val lookupAnotherVehicle = Redirect(routes.VehicleLookup.present())
  protected override val onExit = Redirect(config.startUrl)
}
