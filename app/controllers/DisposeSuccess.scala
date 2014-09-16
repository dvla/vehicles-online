package controllers

import com.google.inject.Inject
import org.joda.time.format.DateTimeFormat
import play.api.mvc.{Action, Controller, Request}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.model.{TraderDetailsModel, VehicleDetailsModel}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import models.DisposeFormModel.{DisposeFormRegistrationNumberCacheKey, DisposeFormTimestampIdCacheKey, DisposeFormTransactionIdCacheKey, DisposeOccurredCacheKey, PreventGoingToDisposePageCacheKey, SurveyRequestTriggerDateCacheKey}
import models.{AllCacheKeys, DisposeCacheKeys, DisposeFormModel, DisposeOnlyCacheKeys, DisposeViewModel}

final class DisposeSuccess @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                     config: Config,
                                     surveyUrl: SurveyUrl,
                                     dateService: DateService) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getModel[TraderDetailsModel],
     request.cookies.getModel[DisposeFormModel],
     request.cookies.getModel[VehicleDetailsModel],
     request.cookies.getString(DisposeFormTransactionIdCacheKey),
     request.cookies.getString(DisposeFormRegistrationNumberCacheKey),
     request.cookies.getString(DisposeFormTimestampIdCacheKey)) match {
       case (Some(traderDetails),
             Some(disposeFormModel),
             Some(vehicleDetails),
             Some(transactionId),
             Some(registrationNumber),
             Some(disposeDateString)) =>
         val disposeViewModel = createViewModel(
           traderDetails,
           disposeFormModel,
           vehicleDetails,
           Some(transactionId),
           registrationNumber
         )
         val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
         val disposeDateTime = formatter.parseDateTime(disposeDateString)
         Ok(views.html.disposal_of_vehicle.dispose_success(disposeViewModel, disposeFormModel, disposeDateTime, surveyUrl(request))).
           discardingCookies(DisposeOnlyCacheKeys) // TODO US320 test for this
       case _ => Redirect(routes.VehicleLookup.present()) // US320 the user has pressed back button after being on dispose-success and pressing new dispose.
     }
  }

  def newDisposal = Action { implicit request =>
    (request.cookies.getModel[TraderDetailsModel], request.cookies.getModel[VehicleDetailsModel]) match {
      case (Some(traderDetails), Some(vehicleDetails)) =>
        Redirect(routes.VehicleLookup.present()).
          discardingCookies(DisposeCacheKeys).
          withCookie(PreventGoingToDisposePageCacheKey, "").
          withCookie(DisposeOccurredCacheKey, "")
      case _ => Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def exit = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present()).
      discardingCookies(AllCacheKeys).
      withCookie(PreventGoingToDisposePageCacheKey, "").
      withCookie(SurveyRequestTriggerDateCacheKey, dateService.now.getMillis.toString)
  }

  private def createViewModel(traderDetails: TraderDetailsModel,
                              disposeFormModel: DisposeFormModel,
                              vehicleDetails: VehicleDetailsModel,
                              transactionId: Option[String],
                              registrationNumber: String): DisposeViewModel =
    DisposeViewModel(
      vehicleMake = vehicleDetails.vehicleMake,
      vehicleModel = vehicleDetails.vehicleModel,
      dealerName = traderDetails.traderName,
      dealerAddress = traderDetails.traderAddress.address,
      transactionId = transactionId,
      registrationNumber = registrationNumber
    )
}

class SurveyUrl @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                          config: Config,
                          dateService: DateService)
  extends (Request[_] => Option[String]) {

  def apply(request: Request[_]): Option[String] = {
    def url = if (!config.prototypeSurveyUrl.trim.isEmpty)
      Some(config.prototypeSurveyUrl.trim)
    else None

    request.cookies.getString(SurveyRequestTriggerDateCacheKey) match {
      case Some(lastSurveyMillis) =>
        if ((lastSurveyMillis.toLong + config.prototypeSurveyPrepositionInterval) < dateService.now.getMillis) url
        else None
      case None => url
    }
  }
}
