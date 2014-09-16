package controllers

import com.google.inject.Inject
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, AnyContent, Call, Controller, Request, Result}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.model.{TraderDetailsModel, VehicleDetailsModel}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding
import webserviceclients.dispose.{DisposalAddressDto, DisposeRequestDto, DisposeResponseDto, DisposeService}
import utils.helpers.Config
import models.DisposeFormModel.Form.{ConsentId, LossOfRegistrationConsentId}
import models.DisposeFormModel.{DisposeFormRegistrationNumberCacheKey, DisposeFormTimestampIdCacheKey, DisposeFormTransactionIdCacheKey, PreventGoingToDisposePageCacheKey}
import models.{DisposeFormModel, DisposeViewModel, VehicleLookupFormModel}
import views.html.disposal_of_vehicle.dispose

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class Dispose @Inject()(webService: DisposeService, dateService: DateService)
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

  private[controllers] val form = Form(
    DisposeFormModel.Form.mapping(dateService)
  )

  def present = Action { implicit request =>
    (request.cookies.getModel[TraderDetailsModel], request.cookies.getString(PreventGoingToDisposePageCacheKey)) match {
      case (Some(traderDetails), None) =>
        request.cookies.getModel[VehicleDetailsModel] match {
          case (Some(vehicleDetails)) =>
            val disposeViewModel = createViewModel(traderDetails, vehicleDetails)
            Ok(dispose(disposeViewModel, form.fill(), dateService))
          case _ => Redirect(routes.VehicleLookup.present())
        }
      case (_, Some(interstitial)) =>
        // US320 Kick them back to the VehicleLookup page if they arrive here by any route other that clicking the
        // "Exit" or "New Dispose" buttons.
        Redirect(routes.VehicleLookup.present()).
          discardingCookie(PreventGoingToDisposePageCacheKey)
      case _ => Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => Future.successful {
        (request.cookies.getModel[TraderDetailsModel], request.cookies.getModel[VehicleDetailsModel]) match {
          case (Some(traderDetails), Some(vehicleDetails)) =>
            val disposeViewModel = createViewModel(traderDetails, vehicleDetails)
            BadRequest(dispose(disposeViewModel, formWithReplacedErrors(invalidForm), dateService))
          case _ =>
            Logger.debug("Could not find expected data in cache on dispose submit - now redirecting...")
            Redirect(routes.SetUpTradeDetails.present())
        }
      },
      validForm => {
        request.cookies.getString(PreventGoingToDisposePageCacheKey) match {
          case Some(_) => Future.successful {
            Redirect(routes.VehicleLookup.present())
          } // US320 prevent user using the browser back button and resubmitting.
          case None =>
            val trackingId = request.cookies.trackingId()
            disposeAction(webService, validForm, trackingId)
        }
      }
    )
  }

  private def formWithReplacedErrors(form: Form[DisposeFormModel])(implicit request: Request[_]) = {
    // When the user doesn't select a value from the drop-down then the mapping will fail to match on an Int before
    // it gets to the constraints, so we need to replace the error type with one that will give a relevant message.
    val dateOfDisposalError = FormError("dateOfDisposal", "error.dateOfDisposal")
    form.replaceError("dateOfDisposal.day", dateOfDisposalError)
      .replaceError("dateOfDisposal.month", dateOfDisposalError)
      .replaceError("dateOfDisposal.year", dateOfDisposalError)
      .replaceError("dateOfDisposal", dateOfDisposalError)
      .replaceError(
        ConsentId,
        "error.required",
        FormError(key = ConsentId, message = "disposal_dispose.consent.notgiven", args = Seq.empty)
      ).replaceError(
        LossOfRegistrationConsentId,
        "error.required",
        FormError(key = LossOfRegistrationConsentId,
        message = "disposal_dispose.loss_of_registration.consent.notgiven",
        args = Seq.empty)
      ).distinctErrors
  }

  private def createViewModel(traderDetails: TraderDetailsModel,
                              vehicleDetails: VehicleDetailsModel): DisposeViewModel =
    DisposeViewModel(
      registrationNumber = vehicleDetails.registrationNumber,
      vehicleMake = vehicleDetails.vehicleMake,
      vehicleModel = vehicleDetails.vehicleModel,
      dealerName = traderDetails.traderName,
      dealerAddress = traderDetails.traderAddress.address)

  private def disposeAction(webService: DisposeService, disposeFormModel: DisposeFormModel, trackingId: String)
                           (implicit request: Request[AnyContent]): Future[Result] = {

    def nextPage(httpResponseCode: Int, response: Option[DisposeResponseDto]) =
    // This makes the choice of which page to go to based on the first one it finds that is not None.
      response match {
        case Some(r) if r.responseCode.isDefined => handleResponseCode(r.responseCode.get)
        case _ => handleHttpStatusCode(httpResponseCode)
      }

    def callMicroService(vehicleLookup: VehicleLookupFormModel, disposeForm: DisposeFormModel, traderDetails: TraderDetailsModel) = {
      val disposeRequest = buildDisposeMicroServiceRequest(vehicleLookup, disposeForm, traderDetails)
      webService.invoke(disposeRequest, trackingId).map {
        case (httpResponseCode, response) =>
          Some(Redirect(nextPage(httpResponseCode, response))).
            map(_.withCookie(disposeFormModel)).
            map(storeResponseInCache(response, _)).
            map(transactionTimestamp).
            map(_.withCookie(PreventGoingToDisposePageCacheKey, "")). // US320 interstitial should redirect to DisposeSuccess.
            get
      }.recover {
        case e: Throwable =>
          Logger.warn(s"Dispose micro-service call failed.", e)
          Redirect(routes.MicroServiceError.present())
      }
    }

    def storeResponseInCache(response: Option[DisposeResponseDto], nextPage: Result): Result =
      response match {
        case Some(o) =>
          val nextPageWithTransactionId =
            if (!o.transactionId.isEmpty) nextPage.withCookie(DisposeFormTransactionIdCacheKey, o.transactionId)
            else nextPage

          if (!o.registrationNumber.isEmpty)
            nextPageWithTransactionId.withCookie(DisposeFormRegistrationNumberCacheKey, o.registrationNumber)
          else nextPageWithTransactionId
        case None => nextPage
      }

    def transactionTimestamp(nextPage: Result) = {
      val transactionTimestamp = dateService.today.toDateTime.get
      val formatter = ISODateTimeFormat.dateTime()
      val isoDateTimeString = formatter.print(transactionTimestamp)
      nextPage.withCookie(DisposeFormTimestampIdCacheKey, isoDateTimeString)
    }

    def buildDisposeMicroServiceRequest(vehicleLookup: VehicleLookupFormModel,
                                        disposeForm: DisposeFormModel,
                                        traderDetails: TraderDetailsModel): DisposeRequestDto = {
      val dateTime = disposeFormModel.dateOfDisposal.toDateTime.get
      val formatter = ISODateTimeFormat.dateTime()
      val isoDateTimeString = formatter.print(dateTime)

      DisposeRequestDto(referenceNumber = vehicleLookup.referenceNumber,
        registrationNumber = vehicleLookup.registrationNumber,
        traderName = traderDetails.traderName,
        traderAddress = DisposalAddressDto.from(traderDetails.traderAddress),
        dateOfDisposal = isoDateTimeString,
        transactionTimestamp = ISODateTimeFormat.dateTime().print(dateService.now.toDateTime),
        prConsent = disposeFormModel.lossOfRegistrationConsent.toBoolean,
        keeperConsent = disposeFormModel.consent.toBoolean,
        mileage = disposeFormModel.mileage
      )
    }

    def handleResponseCode(disposeResponseCode: String): Call =
      disposeResponseCode match {
        case "ms.vehiclesService.response.unableToProcessApplication" =>
          Logger.warn("Dispose soap endpoint redirecting to dispose failure page")
          routes.DisposeFailure.present()
        case "ms.vehiclesService.response.duplicateDisposalToTrade" =>
          Logger.warn("Dispose soap endpoint redirecting to duplicate disposal page")
          routes.DuplicateDisposalError.present()
        case _ =>
          Logger.warn(s"Dispose micro-service failed so now redirecting to micro service error page. " +
            s"Code returned from ms was $disposeResponseCode")
          routes.MicroServiceError.present()
      }

    def handleHttpStatusCode(statusCode: Int): Call =
      statusCode match {
        case OK => routes.DisposeSuccess.present()
        case _ => routes.MicroServiceError.present()
      }

    (request.cookies.getModel[TraderDetailsModel], request.cookies.getModel[VehicleLookupFormModel]) match {
      case (Some(traderDetails), Some(vehicleLookup)) =>
        callMicroService(vehicleLookup, disposeFormModel, traderDetails)
      case _ => Future {
        Logger.error("Could not find either dealer details or VehicleLookupFormModel in cache on Dispose submit")
        Redirect(routes.SetUpTradeDetails.present())
      }
    }
  }
}
