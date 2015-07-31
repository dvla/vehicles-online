package controllers

import com.google.inject.Inject
import models.AllCacheKeys
import models.DisposeCacheKeyPrefix.CookiePrefix
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{mapping, optional}
import play.api.mvc.{Result, Request}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichResult, RichCookies}
import common.controllers.SetUpTradeDetailsBase
import common.LogFormats.logMessage
import common.mappings.BusinessName.businessNameMapping
import common.mappings.Email.email
import common.mappings.Postcode.postcode
import common.model.SetupTradeDetailsFormModel
import common.model.SetupTradeDetailsFormModel.Form.{TraderEmailId, TraderNameId, TraderPostcodeId}
import utils.helpers.Config

class SetUpTradeDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config) extends SetUpTradeDetailsBase with BusinessController {

  protected val submitTarget = controllers.routes.SetUpTradeDetails.submit()
  protected val onSuccess = Redirect(routes.BusinessChooseYourAddress.present())

  override val form = Form(
    mapping(
      TraderNameId -> businessNameMapping,
      TraderPostcodeId -> postcode,
      TraderEmailId -> optional(email)
    )(SetupTradeDetailsFormModel.apply)(SetupTradeDetailsFormModel.unapply)
  )

  override def presentResult(model: Form[SetupTradeDetailsFormModel])(implicit request: Request[_]): Result = {

    Ok(views.html.disposal_of_vehicle.setup_trade_details(model, submitTarget)).
      withNewSession.
      discardingCookies(AllCacheKeys)
  }
  override def invalidFormResult(model: Form[SetupTradeDetailsFormModel])(implicit request: Request[_]): Result =
    BadRequest(views.html.disposal_of_vehicle.setup_trade_details(model, submitTarget))

  override def success(implicit request: Request[_]): Result = {
    Logger.info(s"Redirect to ${onSuccess} - trackingId: ${request.cookies.trackingId()}")
    onSuccess
  }
}
