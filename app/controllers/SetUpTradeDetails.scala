package controllers

import models.AllCacheKeys
import com.google.inject.Inject
import play.api.data.Form
import play.api.mvc.{Result, Request}
import uk.gov.dvla.vehicles.presentation.common.controllers.SetUpTradeDetailsBase
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.model.SetupTradeDetailsFormModel

import utils.helpers.Config
import models.DisposeCacheKeyPrefix.CookiePrefix

class SetUpTradeDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config) extends SetUpTradeDetailsBase {

  override def presentResult(model: Form[SetupTradeDetailsFormModel])(implicit request: Request[_]): Result =
    Ok(views.html.disposal_of_vehicle.setup_trade_details(model)).
      withNewSession.
      discardingCookies(AllCacheKeys)

  override def invalidFormResult(model: Form[SetupTradeDetailsFormModel])(implicit request: Request[_]): Result =
    BadRequest(views.html.disposal_of_vehicle.setup_trade_details(model))

  override def success(implicit request: Request[_]): Result =
    Redirect(routes.BusinessChooseYourAddress.present())
}
