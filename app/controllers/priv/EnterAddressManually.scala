package controllers.priv

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class EnterAddressManually @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends controllers.EnterAddressManually {

  protected override val formTarget = routes.EnterAddressManually.submit()
  protected override val backLink = routes.BusinessChooseYourAddress.present()
  protected override val onCookiesMissing = Redirect(routes.SetUpTradeDetails.present())
  protected override val onSubmitSuccess = Redirect(routes.VehicleLookup.present())
}
