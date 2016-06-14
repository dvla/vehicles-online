package controllers

import com.google.inject.Inject
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.IdentifierCacheKey
import play.api.data.Form
import play.api.mvc.{Action, Request, Result}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.controllers.SetUpTradeDetailsBase
import uk.gov.dvla.vehicles.presentation.common.model.SetupTradeDetailsFormModel
import utils.helpers.Config

class SetUpTradeDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config) extends SetUpTradeDetailsBase with BusinessController {

  protected val submitTarget = controllers.routes.SetUpTradeDetails.submit()
  protected val onSuccess = Redirect(routes.BusinessChooseYourAddress.present())

  private def newSession(result: Result)(implicit request: Request[_]): Result = {
    result
      .withNewSession
      .discardingCookies(AllCacheKeys)
  }

  override def presentResult(model: Form[SetupTradeDetailsFormModel])(implicit request: Request[_]): Result = {
    request.cookies.getString(IdentifierCacheKey) match {
      case Some(c) =>
        Redirect(routes.SetUpTradeDetails.ceg())
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
    logMessage(request.cookies.trackingId(), Info, s"Presenting set up trade details view for identifier $identifier")
    newSession(
      Ok(views.html.disposal_of_vehicle.setup_trade_details(form.fill(), submitTarget))
    ).withCookie(IdentifierCacheKey, identifier)
  }
}
