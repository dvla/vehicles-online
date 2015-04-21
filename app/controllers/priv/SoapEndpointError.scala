package controllers.priv

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class SoapEndpointError @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config) extends controllers.SoapEndpointError {

//  protected override val disposeTarget = routes.Dispose.present()
//  protected override val exitTarget = controllers.routes.BeforeYouStart.present()
}
