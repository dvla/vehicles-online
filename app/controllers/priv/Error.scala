package controllers.priv

import com.google.inject.Inject
import play.api.mvc.Call
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class Error @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                        config: Config) extends controllers.Error with PrivateKeeperController {
  protected override def formTarget(exceptionDigest: String): Call = routes.Error.submit(exceptionDigest)
}
