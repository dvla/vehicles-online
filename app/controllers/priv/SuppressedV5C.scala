package controllers.priv

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class SuppressedV5C @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                config: Config) extends controllers.SuppressedV5C {

  protected override val sellAnotherVehicleTarget = controllers.routes.SuppressedV5C.sellAnotherVehicle()
  protected override val finishTarget = controllers.routes.SuppressedV5C.finish()
  protected override val lookupAnotherVehicle = Redirect(controllers.routes.VehicleLookup.present())
  protected override val onFinish = Redirect(controllers.routes.BeforeYouStart.present())
}
