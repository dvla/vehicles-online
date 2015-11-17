package controllers

import com.google.inject.Inject
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.IdentifierCacheKey
import play.api.data.Form
import play.api.data.Forms.{mapping, optional}
import play.api.mvc.{Action, Request, Result}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.controllers.SetUpTradeDetailsBase
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

  private def newSession(result: Result)(implicit request: Request[_]): Result = {
    result
      .withNewSession
      .discardingCookies(AllCacheKeys)
  }

  override def presentResult(model: Form[SetupTradeDetailsFormModel])(implicit request: Request[_]): Result = {
    request.cookies.getString(IdentifierCacheKey) match {
      case Some(c) =>
        Redirect(routes.SetUpTradeDetails.ceg)
      case None =>
        logMessage(request.cookies.trackingId(), Info, "Presenting set up trade details view")
        newSession(Ok(views.html.disposal_of_vehicle.setup_trade_details(model, submitTarget)))
    }
  }

  override def invalidFormResult(model: Form[SetupTradeDetailsFormModel])(implicit request: Request[_]): Result =
    BadRequest(views.html.disposal_of_vehicle.setup_trade_details(model, submitTarget))

  override def success(implicit request: Request[_]): Result = {
    logMessage(request.cookies.trackingId(), Info, s"Redirect to $onSuccess")
    onSuccess
  }

  def reset = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Info, s"Reset trader details")
    // Call presentResult directly as we don't want the form to be populated
    // before we've had a chance to discard the cookie.
    presentResult(form)
      .discardingCookies(models.TradeDetailsCacheKeys)
  }

  val identifier = "CEG"
  def ceg = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Info, s"Presenting set up trade details view for identifier ${identifier}")
    newSession(
      Ok(views.html.disposal_of_vehicle.setup_trade_details(form.fill(), submitTarget))
    ).withCookie(IdentifierCacheKey, identifier)
  }
}
