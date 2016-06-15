package controllers

import com.google.inject.Inject
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.DisposeViewModel
import org.joda.time.format.DateTimeFormat
import play.api.mvc.{Action, Request}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.LogFormats.DVLALogger
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
  protected val onNewDispose = Redirect("https://www.gov.uk")

  protected val DisposeFormTransactionIdCacheKey = models.DisposeFormModel.DisposeFormTransactionIdCacheKey
  protected val DisposeFormRegistrationNumberCacheKey = models.DisposeFormModel.DisposeFormRegistrationNumberCacheKey
  protected val DisposeFormTimestampIdCacheKey = models.DisposeFormModel.DisposeFormTimestampIdCacheKey
  protected val PreventGoingToDisposePageCacheKey = models.DisposeFormModel.PreventGoingToDisposePageCacheKey
  protected val SurveyRequestTriggerDateCacheKey = models.DisposeFormModel.SurveyRequestTriggerDateCacheKey
  protected val DisposeOccurredCacheKey = models.DisposeFormModel.DisposeOccurredCacheKey

  def present = Action { implicit request =>
    val result = for {
      traderDetails <- request.cookies.getModel[TraderDetailsModel]
      disposeFormModel <- if (isPrivateKeeper) request.cookies.getModel[models.PrivateDisposeFormModel]
                          else request.cookies.getModel[models.DisposeFormModel]
      vehicleDetails <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
      transactionId <- request.cookies.getString(DisposeFormTransactionIdCacheKey)
      registrationNumber <- request.cookies.getString(DisposeFormRegistrationNumberCacheKey)
      disposeDateString <- request.cookies.getString(DisposeFormTimestampIdCacheKey)
    } yield {
      logMessage(request.cookies.trackingId(), Info,
        "User transaction completed successfully - now displaying the dispose success view"
      )
      val disposeViewModel = createViewModel(
        traderDetails,
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
      )).discardingCookies(DisposeOnlyCacheKeys)
    }
    // US320 the user has pressed back button after being on dispose-success and pressing new dispose.
    result getOrElse {
      val msg = "User transaction completed successfully but not displaying the success view " +
        "because the user arrived without all of the required cookies"
      logMessage(request.cookies.trackingId(), Warn, msg)
      onMissingPresentCookies
    }
  }

  def newDisposal = Action { implicit request =>
    val result = for {
      traderDetails <- request.cookies.getModel[TraderDetailsModel]
      vehicleDetails <-request.cookies.getModel[VehicleAndKeeperDetailsModel]
    } yield {
      Redirect(routes.VehicleLookup.present())
        .discardingCookies(DisposeCacheKeys)
        .withCookie(PreventGoingToDisposePageCacheKey, "")
        .withCookie(DisposeOccurredCacheKey, "")
    }
    result getOrElse onMissingNewDisposeCookies
  }

  def exit = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Debug, s"Redirect from DisposeSuccess to $onNewDispose")
    onNewDispose
      .discardingCookies(AllCacheKeys)
      .withCookie(PreventGoingToDisposePageCacheKey, "")
      .withCookie(SurveyRequestTriggerDateCacheKey, dateService.now.getMillis.toString)
  }

  private def createViewModel(traderDetails: TraderDetailsModel,
                              vehicleDetails: VehicleAndKeeperDetailsModel,
                              transactionId: Option[String],
                              registrationNumber: String): DisposeViewModel =
    DisposeViewModel(vehicleDetails, traderDetails, transactionId)
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

    val SurveyRequestTriggerDateCacheKey =
      if (isPrivateKeeper) models.DisposeFormModel.SurveyRequestTriggerDateCacheKey
      else models.PrivateDisposeFormModel.SurveyRequestTriggerDateCacheKey

    request.cookies.getString(SurveyRequestTriggerDateCacheKey) match {
      case Some(lastSurveyMillis) =>
        if ((lastSurveyMillis.toLong + config.prototypeSurveyPrepositionInterval) < dateService.now.getMillis) {
          logMessage(request.cookies.trackingId(), Debug, s"Redirecting to survey $url")
          url
        }
        else None
      case None =>
        logMessage(request.cookies.trackingId(), Debug, s"Redirecting to survey $url")
        url
    }
  }
}
