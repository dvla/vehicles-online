package controllers

import com.google.inject.Inject
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.VehicleLookupFormModel
import models.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey
import play.api.Logger
import play.api.mvc.{Request, Result}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import common.controllers.VehicleLookupFailureBase
import common.LogFormats.logMessage
import common.model.TraderDetailsModel
import utils.helpers.Config

class VehicleLookupFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config)
  extends VehicleLookupFailureBase[VehicleLookupFormModel] with BusinessController {

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

  override def missingPresentCookieDataResult()(implicit request: Request[_]): Result = {
    Logger.error(s"Failed to find cookie details, redirecting to ${routes.SetUpTradeDetails.present()} - " +
      s"trackingId: ${request.cookies.trackingId()}")
    missingPresentCookieData
  }

  override def submitResult()(implicit request: Request[_]): Result =
    request.cookies.getModel[TraderDetailsModel] match {
      case Some(dealerDetails) => {
        Logger.info(s"Failed to find vehicle details, redirecting to ${routes.VehicleLookup.present()} - " +
          s"trackingId: ${request.cookies.trackingId()}")
        success
      }
      case _ => {
        Logger.info(s"Failed to find dealer details, redirecting to ${routes.SetUpTradeDetails.present()} - " +
          s"trackingId: ${request.cookies.trackingId()}")
        missingSubmitCookieDataResult
      }
    }

  override def missingSubmitCookieDataResult()(implicit request: Request[_]): Result = {
    Logger.error(s"Failed to find cookie details, redirecting to ${routes.BeforeYouStart.present()} - " +
      s"trackingId: ${request.cookies.trackingId()}")
    missingSubmitCookieData
  }
}
