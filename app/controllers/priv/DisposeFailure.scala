package controllers.priv

import com.google.inject.Inject
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.DisposeFormModelPrivate
import models.DisposeFormModelPrivate.DisposeFormTransactionIdCacheKey
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import common.LogFormats.logMessage
import common.model.{DisposeModel, TraderDetailsModel, VehicleAndKeeperDetailsModel}
import utils.helpers.Config

class DisposeFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                 config: Config) extends Controller {

  protected val sellNewVehicleCall = routes.VehicleLookup.present()
  protected val exitCall = routes.SetUpTradeDetails.present()
  protected val onMissingCookies = Redirect(routes.SetUpTradeDetails.present())

  def present = Action { implicit request =>
    val result = for {
      dealerDetails <- request.cookies.getModel[TraderDetailsModel]
      disposeFormModel <- request.cookies.getModel[DisposeFormModelPrivate]
      vehicleDetails <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
      transactionId <- request.cookies.getString(DisposeFormTransactionIdCacheKey)
    } yield {
        Logger.info(s"Presenet disposeFailure page - trackingId: ${request.cookies.trackingId()}")
        val disposeViewModel = createViewModel(dealerDetails, vehicleDetails, Some(transactionId))
        Ok(views.html.disposal_of_vehicle.dispose_failure_private(
          disposeViewModel.transactionId,
          disposeFormModel,
          sellNewVehicleCall,
          exitCall
        ))
      }

    result getOrElse {
      Logger.debug(s"Could not find all expected data in cache on dispose failure present, " +
        s"redirecting to ${onMissingCookies} - trackingId: ${request.cookies.trackingId()}")
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

