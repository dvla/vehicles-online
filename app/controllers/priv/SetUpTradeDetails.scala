package controllers.priv

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import utils.helpers.Config

class SetUpTradeDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config)
  extends controllers.SetUpTradeDetails with PrivateKeeperController {

  protected override val submitTarget = routes.SetUpTradeDetails.submit()
  protected override val onSuccess = Redirect(routes.BusinessChooseYourAddress.present())

  override def reset = play.api.mvc.Action { implicit request =>
    Ok(views.html.disposal_of_vehicle.setup_trade_details(form, submitTarget))
      .discardingCookies(models.TradeDetailsCacheKeys)
  }
}
