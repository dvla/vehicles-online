package controllers.priv

import javax.inject.Inject
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.services.DateService
import utils.helpers.Config

class DuplicateDisposalError @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                         config: Config,
                                         dateService: DateService)
  extends controllers.DuplicateDisposalError with PrivateKeeperController {

  protected override val tryAgainLink = routes.VehicleLookup.present()
  protected override val exitLink = controllers.routes.BeforeYouStart.present()
}
