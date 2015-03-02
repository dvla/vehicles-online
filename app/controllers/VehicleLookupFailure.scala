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

  override val vehicleLookupResponseCodeCacheKey: String = VehicleLookupResponseCodeCacheKey

  override def presentResult(model: VehicleLookupFormModel, responseCode: String)(implicit request: Request[_]): Result =
    request.cookies.getModel[TraderDetailsModel] match {
      case Some(dealerDetails) =>
        Ok(views.html.disposal_of_vehicle.vehicle_lookup_failure(
          data = model,
          responseCodeVehicleLookupMSErrorMessage = responseCode)
        )
      case _ => missingPresentCookieDataResult
    }

  override def missingPresentCookieDataResult()(implicit request: Request[_]): Result =
    Redirect(routes.SetUpTradeDetails.present())

  override def submitResult()(implicit request: Request[_]): Result =
    request.cookies.getModel[TraderDetailsModel] match {
      case Some(dealerDetails) => Redirect(routes.VehicleLookup.present())
      case _ => missingSubmitCookieDataResult
    }

  override def missingSubmitCookieDataResult()(implicit request: Request[_]): Result =
    Redirect(routes.BeforeYouStart.present())
}
