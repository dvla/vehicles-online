package controllers.priv

import javax.inject.Inject

import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class DuplicateDisposalError @Inject()()
                                      (implicit clientSideSessionFactory: ClientSideSessionFactory, config: Config)
  extends controllers.DuplicateDisposalError {
  protected override val tryAgainLink = controllers.routes.VehicleLookup.present()
  protected override val exitLink = controllers.routes.BeforeYouStart.present()
}