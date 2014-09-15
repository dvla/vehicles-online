package controllers

import com.google.inject.Inject
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Controller, DiscardingCookie, Request}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.model.{TraderDetailsModel, BruteForcePreventionModel}
import utils.helpers.Config
import viewmodels.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey
import viewmodels.VehicleLookupFormModel

final class VehicleLookupFailure @Inject()()
                                 (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                  config: Config) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getModel[TraderDetailsModel],
      request.cookies.getModel[BruteForcePreventionModel],
      request.cookies.getModel[VehicleLookupFormModel],
      request.cookies.getString(VehicleLookupResponseCodeCacheKey)) match {
      case (Some(dealerDetails),
            Some(bruteForcePreventionResponse),
            Some(vehicleLookUpFormModelDetails),
            Some(vehicleLookupResponseCode)) =>
        displayVehicleLookupFailure(
          vehicleLookUpFormModelDetails,
          bruteForcePreventionResponse,
          vehicleLookupResponseCode
        )
      case _ => Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def submit = Action { implicit request =>
    (request.cookies.getModel[TraderDetailsModel], request.cookies.getModel[VehicleLookupFormModel]) match {
      case (Some(dealerDetails), Some(vehicleLookUpFormModelDetails)) =>
        Logger.debug("Found dealer and vehicle details")
        Redirect(routes.VehicleLookup.present())
      case _ => Redirect(routes.BeforeYouStart.present())
    }
  }

  private def displayVehicleLookupFailure(vehicleLookUpFormModelDetails: VehicleLookupFormModel,
                                          bruteForcePreventionViewModel: BruteForcePreventionModel,
                                          vehicleLookupResponseCode: String)(implicit request: Request[AnyContent]) = {
    Ok(views.html.disposal_of_vehicle.vehicle_lookup_failure(
      data = vehicleLookUpFormModelDetails,
      responseCodeVehicleLookupMSErrorMessage = vehicleLookupResponseCode)
    ).
    discardingCookies(DiscardingCookie(name = VehicleLookupResponseCodeCacheKey))
  }
}
