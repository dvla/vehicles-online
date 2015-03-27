package controllers.priv

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class VehicleLookupFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends controllers.VehicleLookupFailure {

  protected override val tryAgainTarget = controllers.routes.VehicleLookup.present()
  protected override val exitTarget = controllers.routes.BeforeYouStart.present()
  protected override val missingPresentCookieData = Redirect(controllers.routes.SetUpTradeDetails.present())
  protected override val missingSubmitCookieData = Redirect(controllers.routes.BeforeYouStart.present())
  protected override val success = Redirect(controllers.routes.VehicleLookup.present())
}
