package controllers

import com.google.inject.Inject
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.DisposeFormModel
import models.DisposeFormModel.DisposeFormTransactionIdCacheKey
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.model.{DisposeModel, TraderDetailsModel, VehicleAndKeeperDetailsModel}
import utils.helpers.Config

class DisposeFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                 config: Config) extends Controller {
  protected val sellNewVehicleCall = controllers.routes.VehicleLookup.present()
  protected val exitCall = controllers.routes.SetUpTradeDetails.present()
  protected val onMissingCookies = Redirect(routes.SetUpTradeDetails.present())

  def present = Action { implicit request =>
    val result = for {
      dealerDetails <- request.cookies.getModel[TraderDetailsModel]
      disposeFormModel <- request.cookies.getModel[DisposeFormModel]
      vehicleDetails <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
      transactionId <- request.cookies.getString(DisposeFormTransactionIdCacheKey)
    } yield {
      val disposeViewModel = createViewModel(dealerDetails, vehicleDetails, Some(transactionId))
      Ok(views.html.disposal_of_vehicle.dispose_failure(
        disposeViewModel.transactionId,
        disposeFormModel,
        sellNewVehicleCall,
        exitCall
      ))
    }

    result getOrElse {
      Logger.debug(s"Could not find all expected data in cache on dispose failure present, " +
        s"redirecting - trackingId: ${request.cookies.trackingId()}")
      onMissingCookies
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
