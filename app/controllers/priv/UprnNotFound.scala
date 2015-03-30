package controllers.priv

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class UprnNotFound @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                               config: Config) extends controllers.UprnNotFound {

  protected override val enterAddressManuallyTarget = controllers.routes.EnterAddressManually.present()
  protected override val setupTradeDetailsTarget = controllers.routes.SetUpTradeDetails.present()
}