package controllers

import com.google.inject.Inject
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.DisposeFormModel.DisposeFormRegistrationNumberCacheKey
import models.DisposeFormModel.DisposeFormTimestampIdCacheKey
import models.DisposeFormModel.DisposeFormTransactionIdCacheKey
import models.DisposeFormModel.DisposeOccurredCacheKey
import models.DisposeFormModel.PreventGoingToDisposePageCacheKey
import models.DisposeFormModel.SurveyRequestTriggerDateCacheKey
import models.{AllCacheKeys, DisposeCacheKeys, DisposeFormModel, DisposeOnlyCacheKeys, DisposeViewModel}
import org.joda.time.format.DateTimeFormat
import play.api.mvc.{Action, Request}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import common.model.{TraderDetailsModel, VehicleAndKeeperDetailsModel}
import common.services.DateService
import utils.helpers.Config

class DisposeSuccess @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                               config: Config,
                               surveyUrl: SurveyUrl,
                               dateService: DateService) extends BusinessController {
  protected val newDisposeFormTarget = controllers.routes.DisposeSuccess.newDisposal()
  protected val exitDisposeFormTarget = controllers.routes.DisposeSuccess.exit()
  protected val onMissingPresentCookies = Redirect(routes.VehicleLookup.present())
  protected val onMissingNewDisposeCookies = Redirect(routes.SetUpTradeDetails.present())
  protected val onNewDispose = Redirect(routes.BeforeYouStart.present())

  def present = Action { implicit request =>

    val disposeFormModelOpt = request
    val result = for {
      traderDetails <- request.cookies.getModel[TraderDetailsModel]
      disposeFormModel <- request.cookies.getModel[DisposeFormModel]
      vehicleDetails <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
      transactionId <- request.cookies.getString(DisposeFormTransactionIdCacheKey)
      registrationNumber <- request.cookies.getString(DisposeFormRegistrationNumberCacheKey)
      disposeDateString <- request.cookies.getString(DisposeFormTimestampIdCacheKey)
    } yield {
        logMessage(request.cookies.trackingId(), Info, "Dispose success page")
      val disposeViewModel = createViewModel(
        traderDetails,
        disposeFormModel,
        vehicleDetails,
        Some(transactionId),
        registrationNumber
      )
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
      val disposeDateTime = formatter.parseDateTime(disposeDateString)
      Ok(views.html.disposal_of_vehicle.dispose_success(
        disposeViewModel,
        disposeFormModel,
        disposeDateTime,
        surveyUrl(request, isPrivateKeeper = isPrivateKeeper),
        newDisposeFormTarget,
        exitDisposeFormTarget
      )).discardingCookies(DisposeOnlyCacheKeys) // TODO US320 test for this
    }

    result getOrElse onMissingPresentCookies // US320 the user has pressed back button after being on dispose-success and pressing new dispose.
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
                              disposeFormModel: DisposeFormModel,
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

class SurveyUrl @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                          config: Config,
                          dateService: DateService)
  extends ((Request[_], Boolean) => Option[String]) with DVLALogger {

  def apply(request: Request[_], isPrivateKeeper: Boolean): Option[String] = {
    def url =
      if (isPrivateKeeper)
        if (!config.privateKeeperSurveyUrl.trim.isEmpty) Some(config.privateKeeperSurveyUrl.trim) else None
      else
        if (!config.surveyUrl.trim.isEmpty) Some(config.surveyUrl.trim) else None

    request.cookies.getString(SurveyRequestTriggerDateCacheKey) match {
      case Some(lastSurveyMillis) =>
        if ((lastSurveyMillis.toLong + config.prototypeSurveyPrepositionInterval) < dateService.now.getMillis) {
          logMessage(request.cookies.trackingId(), Debug, s"Redirecting to survey $url")
          url
        }
        else None
      case None =>
        logMessage(request.cookies.trackingId(), Debug,s"Redirecting to survey $url")
        url
    }
  }
}
