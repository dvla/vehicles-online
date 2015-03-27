package controllers.priv

import com.google.inject.Inject
import controllers.routes
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class VehicleLookupFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends controllers.VehicleLookupFailure {

  protected override val tryAgainTarget = controllers.routes.VehicleLookup.present()
  protected override val exitTarget = controllers.routes.BeforeYouStart.present()
  protected override val missingPresentCookieData = Redirect(routes.SetUpTradeDetails.present())
  protected override val missingSubmitCookieData = Redirect(routes.BeforeYouStart.present())
  protected override val success = Redirect(routes.VehicleLookup.present())
}
