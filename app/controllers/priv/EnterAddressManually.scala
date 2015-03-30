package controllers.priv

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class EnterAddressManually @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends controllers.EnterAddressManually {

  protected override val formTarget = controllers.routes.EnterAddressManually.submit()
  protected override val backLink = controllers.routes.BusinessChooseYourAddress.present()
  protected override val onCookiesMissing = Redirect(controllers.routes.SetUpTradeDetails.present())
  protected override val onSubmitSuccess = Redirect(controllers.routes.VehicleLookup.present())
}
