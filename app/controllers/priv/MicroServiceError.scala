package controllers.priv

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class MicroServiceError @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                  config: Config) extends controllers.MicroServiceError {

//  protected override val defaultRedirectUrl = routes.VehicleLookup.present().url
//  protected override val tryAgainTarget = routes.MicroServiceError.back()
//  protected override val exitTarget = controllers.routes.BeforeYouStart.present()
}
