package controllers

import com.google.inject.Inject
import models.DisposeFormModel.Form.{ConsentId, LossOfRegistrationConsentId, MileageId}
import models.DisposeFormModel.DisposeFormRegistrationNumberCacheKey
import models.DisposeFormModel.DisposeFormTimestampIdCacheKey
import models.DisposeFormModel.DisposeFormTransactionIdCacheKey
import models.DisposeFormModel.PreventGoingToDisposePageCacheKey
import models.{DisposeFormModel, DisposeViewModel, VehicleLookupFormModel, DisposeCacheKeys}
import models.DisposeCacheKeyPrefix.CookiePrefix
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, AnyContent, Call, Controller, Request, Result}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.model.{TraderDetailsModel, VehicleAndKeeperDetailsModel}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config
import views.html.disposal_of_vehicle.dispose
import webserviceclients.dispose.{DisposalAddressDto, DisposeRequestDto, DisposeResponseDto, DisposeService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Dispose @Inject()(webService: DisposeService, dateService: DateService)
                       (implicit clientSideSessionFactory: ClientSideSessionFactory,
                        config: Config) extends Controller {

  private[controllers] val form = Form(
    DisposeFormModel.Form.mapping(dateService)
  )

  protected val formTarget = controllers.routes.Dispose.submit()
  protected val backLink = controllers.routes.VehicleLookup.present()
  protected val vehicleDetailsMissing = Redirect(routes.VehicleLookup.present())
  protected val onVehicleAlreadyDisposed = Redirect(routes.VehicleLookup.present())
  protected val onTraderDetailsMissing = Redirect(routes.SetUpTradeDetails.present())
  protected val microserviceErrorCall = routes.MicroServiceError.present()
  protected val onMicroserviceError = Redirect(routes.MicroServiceError.present())
  protected val onDisposeFailure = routes.DisposeFailure.present()
  protected val onDuplicateDispose = routes.DuplicateDisposalError.present()
  protected val onDisposeSuccess = routes.DisposeSuccess.present()

  def present = Action { implicit request =>
    (request.cookies.getModel[TraderDetailsModel], request.cookies.getString(PreventGoingToDisposePageCacheKey)) match {
      case (Some(traderDetails), None) =>
        request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
          case (Some(vehicleDetails)) =>
            val disposeViewModel = createViewModel(traderDetails, vehicleDetails)
            Ok(dispose(disposeViewModel, form.fill(), dateService, formTarget, backLink))
          case _ => vehicleDetailsMissing
        }
      case (_, Some(interstitial)) =>
        // US320 Kick them back to the VehicleLookup page if they arrive here by any route other that clicking the
        // "Exit" or "New Dispose" buttons.
        onVehicleAlreadyDisposed.
          discardingCookie(PreventGoingToDisposePageCacheKey).
        discardingCookies(DisposeCacheKeys)
      case _ => onTraderDetailsMissing
    }
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => Future.successful {
        val result = for {
          traderDetails <- request.cookies.getModel[TraderDetailsModel]
          vehicleDetails <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
        } yield {
          val disposeViewModel = createViewModel(traderDetails, vehicleDetails)
          BadRequest(dispose(disposeViewModel, formWithReplacedErrors(invalidForm), dateService, formTarget, backLink))
        }

        result getOrElse {
          Logger.debug(s"Could not find expected data in cache on dispose submit - now redirecting... " +
            s"- trackingId: ${request.cookies.trackingId()}")
          Redirect(routes.SetUpTradeDetails.present())
        }
      },
      validForm => {
        request.cookies.getString(PreventGoingToDisposePageCacheKey) match {
          case Some(_) => Future.successful(onVehicleAlreadyDisposed) // US320 prevent user using the browser back button and resubmitting.
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
        MileageId,
        "error.number",
        FormError(key = MileageId, message = "disposal_dispose.mileage.validation", args = Seq.empty)
      ).replaceError(
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
                              vehicleDetails: VehicleAndKeeperDetailsModel): DisposeViewModel =
    DisposeViewModel(
      registrationNumber = vehicleDetails.registrationNumber,
      vehicleMake = vehicleDetails.make,
      vehicleModel = vehicleDetails.model,
      dealerName = traderDetails.traderName,
      dealerAddress = traderDetails.traderAddress.address
    )

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
          Logger.warn(s"Dispose micro-service call failed. - trackingId: ${request.cookies.trackingId()}", e)
          onMicroserviceError
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
      val dateTime = disposeFormModel.dateOfDisposal.toDateTimeAtStartOfDay
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

    def handleResponseCode(disposeResponseCode: String)(implicit request: Request[_]): Call =
      disposeResponseCode match {
        case "ms.vehiclesService.response.unableToProcessApplication" =>
          Logger.warn(s"Dispose soap endpoint redirecting to dispose " +
          s"failure page - trackingId: ${request.cookies.trackingId()}")
          onDisposeFailure
        case "ms.vehiclesService.response.duplicateDisposalToTrade" =>
          Logger.warn(s"Dispose soap endpoint redirecting to duplicate disposal page" +
            " - trackingId: ${request.cookies.trackingId()}")
          onDuplicateDispose
        case _ =>
          Logger.warn(s"Dispose micro-service failed so now redirecting to micro service error page. " +
            s"Code returned from ms was $disposeResponseCode  - trackingId: ${request.cookies.trackingId()}")
          microserviceErrorCall
      }

    def handleHttpStatusCode(statusCode: Int): Call =
      statusCode match {
        case OK => onDisposeSuccess
        case _ => microserviceErrorCall
      }

    (request.cookies.getModel[TraderDetailsModel], request.cookies.getModel[VehicleLookupFormModel]) match {
      case (Some(traderDetails), Some(vehicleLookup)) =>
        callMicroService(vehicleLookup, disposeFormModel, traderDetails)
      case _ => Future {
        Logger.error(s"Could not find either dealer details or VehicleLookupFormModel " +
          s"in cache on Dispose submit  - trackingId: ${request.cookies.trackingId()}")
        onTraderDetailsMissing
      }
    }
  }
}
