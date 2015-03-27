package controllers

import com.google.inject.Inject
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.VehicleLookupFormModel
import models.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey
import play.api.mvc.{Request, Result}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupFailureBase
import uk.gov.dvla.vehicles.presentation.common.model.TraderDetailsModel
import utils.helpers.Config

class VehicleLookupFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends VehicleLookupFailureBase[VehicleLookupFormModel] {

  protected val tryAgainTarget = controllers.routes.VehicleLookup.present()
  protected val exitTarget = controllers.routes.BeforeYouStart.present()
  protected val missingPresentCookieData = Redirect(routes.SetUpTradeDetails.present())
  protected val missingSubmitCookieData = Redirect(routes.BeforeYouStart.present())
  protected val success = Redirect(routes.VehicleLookup.present())
  override val vehicleLookupResponseCodeCacheKey: String = VehicleLookupResponseCodeCacheKey

  override def presentResult(model: VehicleLookupFormModel, responseCode: String)(implicit request: Request[_]): Result =
    request.cookies.getModel[TraderDetailsModel] match {
      case Some(dealerDetails) =>
        Ok(views.html.disposal_of_vehicle.vehicle_lookup_failure(
          data = model,
          responseCodeVehicleLookupMSErrorMessage = responseCode,
          tryAgainTarget,
          exitTarget)
        )
      case _ => missingPresentCookieDataResult
    }

  override def missingPresentCookieDataResult()(implicit request: Request[_]): Result =
    missingPresentCookieData

  override def submitResult()(implicit request: Request[_]): Result =
    request.cookies.getModel[TraderDetailsModel] match {
      case Some(dealerDetails) => success
      case _ => missingSubmitCookieDataResult
    }

  override def missingSubmitCookieDataResult()(implicit request: Request[_]): Result =
    missingSubmitCookieData
}
