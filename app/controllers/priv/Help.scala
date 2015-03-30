package controllers.priv

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class Help @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                       config: Config) extends controllers.Help {

  protected override val backTarget = routes.Help.back()
  protected override val exitTarget = controllers.routes.BeforeYouStart.present()
}
