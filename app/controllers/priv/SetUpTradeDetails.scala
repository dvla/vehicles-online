package controllers.priv

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class SetUpTradeDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config) extends controllers.SetUpTradeDetails {

//  protected override val submitTarget = routes.SetUpTradeDetails.submit()
//  protected override val onSuccess = Redirect(routes.BusinessChooseYourAddress.present())
}
