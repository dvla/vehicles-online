package controllers.priv

import controllers.SurveyUrl
import com.google.inject.Inject
import models.AllCacheKeys
import models.DisposeCacheKeys
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.DisposeFormModelPrivate.DisposeFormRegistrationNumberCacheKey
import models.DisposeFormModelPrivate.DisposeFormTimestampIdCacheKey
import models.DisposeFormModelPrivate.DisposeFormTransactionIdCacheKey
import models.DisposeFormModelPrivate.DisposeOccurredCacheKey
import models.DisposeFormModelPrivate.PreventGoingToDisposePageCacheKey
import models.DisposeFormModelPrivate.SurveyRequestTriggerDateCacheKey
import models.DisposeFormModelPrivate
import models.DisposeOnlyCacheKeys
import models.DisposeViewModel
import org.joda.time.format.DateTimeFormat
import play.api.mvc.Action
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.model.{TraderDetailsModel, VehicleAndKeeperDetailsModel}
import common.services.DateService
import utils.helpers.Config

class DisposeSuccess @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                               config: Config,
                               surveyUrl: SurveyUrl,
                               dateService: DateService) extends PrivateKeeperController {

  protected val newDisposeFormTarget = routes.DisposeSuccess.newDisposal()
  protected val exitDisposeFormTarget = routes.DisposeSuccess.exit()
  protected val onMissingPresentCookies = Redirect(routes.VehicleLookup.present())
  protected val onMissingNewDisposeCookies = Redirect(routes.SetUpTradeDetails.present())
  protected val onNewDispose = Redirect(controllers.routes.BeforeYouStart.present())

  def present = Action { implicit request =>

    val result = for {
      traderDetails <- request.cookies.getModel[TraderDetailsModel]
      disposeFormModel <- request.cookies.getModel[DisposeFormModelPrivate]
      vehicleDetails <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
      transactionId <- request.cookies.getString(DisposeFormTransactionIdCacheKey)
      registrationNumber <- request.cookies.getString(DisposeFormRegistrationNumberCacheKey)
      disposeDateString <- request.cookies.getString(DisposeFormTimestampIdCacheKey)
    } yield {
        logMessage(request.cookies.trackingId(), Info, "Dispose success page")
        val disposeViewModel = createViewModel(
          traderDetails,
          //disposeFormModel,
          vehicleDetails,
          Some(transactionId),
          registrationNumber
        )
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        val disposeDateTime = formatter.parseDateTime(disposeDateString)
        Ok(views.html.disposal_of_vehicle.dispose_success_private(
          disposeViewModel,
          disposeFormModel,
          disposeDateTime,
          surveyUrl(request, isPrivateKeeper = isPrivateKeeper),
          newDisposeFormTarget,
          exitDisposeFormTarget
        )).discardingCookies(DisposeOnlyCacheKeys) // TODO US320 test for this
      }

    // US320 the user has pressed back button after being on dispose-success and pressing new dispose.
    result getOrElse onMissingPresentCookies
  }

  def newDisposal = Action { implicit request =>
    val result = for {
      traderDetails <- request.cookies.getModel[TraderDetailsModel]
      vehicleDetails <-request.cookies.getModel[VehicleAndKeeperDetailsModel]
    } yield {
        Redirect(routes.VehicleLookup.present()).
          discardingCookies(DisposeCacheKeys).
          withCookie(PreventGoingToDisposePageCacheKey, "").
          withCookie(DisposeOccurredCacheKey, "")
      }
    result getOrElse onMissingNewDisposeCookies
  }

  def exit = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Debug, s"Redirect from DisposeSuccess to $onNewDispose")
    onNewDispose.
      discardingCookies(AllCacheKeys).
      withCookie(PreventGoingToDisposePageCacheKey, "").
      withCookie(SurveyRequestTriggerDateCacheKey, dateService.now.getMillis.toString)
  }

  private def createViewModel(traderDetails: TraderDetailsModel,
                              vehicleDetails: VehicleAndKeeperDetailsModel,
                              transactionId: Option[String],
                              registrationNumber: String): DisposeViewModel =
    DisposeViewModel(
      vehicleMake = vehicleDetails.make,
      vehicleModel = vehicleDetails.model,
      dealerName = traderDetails.traderName,
      dealerAddress = traderDetails.traderAddress.address,
      transactionId = transactionId,
      registrationNumber = registrationNumber
    )
}