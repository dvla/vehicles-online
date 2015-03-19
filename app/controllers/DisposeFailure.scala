package controllers

import com.google.inject.Inject
import models.DisposeFormModel.DisposeFormTransactionIdCacheKey
import models.DisposeFormModel
import models.DisposeCacheKeyPrefix.CookiePrefix
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.model.{DisposeModel, TraderDetailsModel, VehicleAndKeeperDetailsModel}
import utils.helpers.Config

class DisposeFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getModel[TraderDetailsModel],
     request.cookies.getModel[DisposeFormModel],
     request.cookies.getModel[VehicleAndKeeperDetailsModel],
     request.cookies.getString(DisposeFormTransactionIdCacheKey)) match {
      case (Some(dealerDetails), Some(disposeFormModel), Some(vehicleDetails), Some(transactionId)) =>
        val disposeViewModel = createViewModel(dealerDetails, vehicleDetails, Some(transactionId))
        Ok(views.html.disposal_of_vehicle.dispose_failure(disposeViewModel.transactionId, disposeFormModel))
      case _ =>
        Logger.debug(s"Could not find all expected data in cache on dispose failure present, " +
          s"redirecting - trackingId: ${request.cookies.trackingId()}")
        Redirect(routes.SetUpTradeDetails.present())
    }
  }

  private def createViewModel(traderDetails: TraderDetailsModel,
                              vehicleDetails: VehicleAndKeeperDetailsModel,
                              transactionId: Option[String]): DisposeModel =
    DisposeModel(
      registrationNumber = vehicleDetails.registrationNumber,
      vehicleMake = vehicleDetails.make.getOrElse(""),
      vehicleModel = vehicleDetails.model.getOrElse(""),
      dealerName = traderDetails.traderName,
      dealerAddress = traderDetails.traderAddress,
      transactionId = transactionId
    )
}
