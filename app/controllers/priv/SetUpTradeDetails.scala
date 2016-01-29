package controllers.priv

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import utils.helpers.Config
import common.mappings.BusinessName.businessNameMapping
import common.mappings.Email.email
import common.mappings.Postcode.postcode
import common.model.SetupTradeDetailsFormModel
import common.model.SetupTradeDetailsFormModel.Form.{TraderEmailId, TraderNameId, TraderPostcodeId}
import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

class SetUpTradeDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config)
  extends controllers.SetUpTradeDetails with PrivateKeeperController {

  protected override val submitTarget = routes.SetUpTradeDetails.submit()
  protected override val onSuccess = Redirect(routes.BusinessChooseYourAddress.present())

  override def reset = play.api.mvc.Action { implicit request =>
    Ok(views.html.disposal_of_vehicle.setup_trade_details(form, submitTarget))
      .discardingCookies(models.TradeDetailsCacheKeys)
  }

    override val form = Form(
    mapping(
      TraderNameId -> businessNameMapping,
      TraderPostcodeId -> postcode,
      TraderEmailId -> optional(email)
    )(SetupTradeDetailsFormModel.apply)(SetupTradeDetailsFormModel.unapply)
  )

}
