package controllers

import com.google.inject.Inject
import models.DisposeFormModel.Form.{ConsentId, LossOfRegistrationConsentId, MileageId}
import models.DisposeFormModel.DisposeFormRegistrationNumberCacheKey
import models.DisposeFormModel.DisposeFormTimestampIdCacheKey
import models.DisposeFormModel.DisposeFormTransactionIdCacheKey
import models.DisposeFormModel.PreventGoingToDisposePageCacheKey
import models.{DisposeFormModel, DisposeViewModel, VehicleLookupFormModel, DisposeCacheKeys}
import models.DisposeCacheKeyPrefix.CookiePrefix
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.ISODateTimeFormat
import play.api.data.{Form, FormError}
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Call, Controller, Request, Result}
import uk.gov.dvla.vehicles.presentation.common
import webserviceclients.emailservice.EmailService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{TrackingId, ClientSideSessionFactory}
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.LogFormats.anonymize
import common.model.{TraderDetailsModel, VehicleAndKeeperDetailsModel}
import uk.gov.dvla.vehicles.presentation.common.services.{SEND, DateService}
import common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config
import views.html.disposal_of_vehicle.dispose
import webserviceclients.dispose.{DisposalAddressDto, DisposeRequestDto, DisposeResponseDto, DisposeService}

class Dispose @Inject()(webService: DisposeService, emailService: EmailService, dateService: DateService)
                       (implicit clientSideSessionFactory: ClientSideSessionFactory,
                        config: Config) extends BusinessController  {

  def form = Form(
    DisposeFormModel.Form.mapping(dateService)
  )

  protected val formTarget= controllers.routes.Dispose.submit()
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
          case _ => {
            logMessage(request.cookies.trackingId(), Error, s"Failed to find vehicle details, redirecting to ${vehicleDetailsMissing}")
            vehicleDetailsMissing
          }
        }
      case (_, Some(interstitial)) => {
        // US320 Kick them back to the VehicleLookup page if they arrive here by any route other that clicking the
        // "Exit" or "New Dispose" buttons.
        logMessage(request.cookies.trackingId(), Error,s"Vehicle ids already dispose, redirecting to ${onVehicleAlreadyDisposed}")
        onVehicleAlreadyDisposed.
          discardingCookie(PreventGoingToDisposePageCacheKey).
          discardingCookies(DisposeCacheKeys)
      }
      case _ => {
        logMessage(request.cookies.trackingId(), Error,s"Failed to find dealer details, redirecting to ${onTraderDetailsMissing}")
        onTraderDetailsMissing
      }
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
          BadRequest(dispose(
            disposeViewModel,
            formWithReplacedErrors(invalidForm),
            dateService,
            formTarget,
            backLink
          ))
        }

        result getOrElse {
          logMessage(request.cookies.trackingId(), Error,s"Could not find expected data in cache on dispose submit - now redirecting...")
          Redirect(routes.SetUpTradeDetails.present())
        }
      },
      validForm => {
        request.cookies.getString(PreventGoingToDisposePageCacheKey) match {
          case Some(_) => {
            logMessage(request.cookies.trackingId(), Error,s"Vehicle ids already dispose, redirecting to ${onVehicleAlreadyDisposed}")
            Future.successful(onVehicleAlreadyDisposed)
          } // US320 prevent user using the browser back button and resubmitting.
          case None =>
            doDisposeAction(webService, validForm)
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

  private def doDisposeAction(webService: DisposeService, disposeFormModel: DisposeFormModel)
                                (implicit request: Request[AnyContent]): Future[Result] = {

    def nextPage(httpResponseCode: Int, response: Option[DisposeResponseDto], disposeRequest: DisposeRequestDto) =
    // This makes the choice of which page to go to based on the first one it finds that is not None.
      response match {
        case Some(r) if r.responseCode.isDefined => handleResponseCode(r.responseCode.get,
          response.map(_.transactionId).getOrElse(""), disposeRequest)
        case _ => handleHttpStatusCode(httpResponseCode)
      }

    def callMicroService(vehicleLookup: VehicleLookupFormModel, disposeForm: DisposeFormModel, traderDetails: TraderDetailsModel) = {
      val disposeRequest = buildDisposeMicroServiceRequest(vehicleLookup, disposeForm, traderDetails)
      logMessage(request.cookies.trackingId(), Info, s"Call Dispose micro-service")

      logMessage( request.cookies.trackingId(), Debug,"Dispose micro-service request",
        Some(Seq(disposeRequest.dateOfDisposal,
          disposeRequest.keeperConsent.toString,
          disposeRequest.mileage.toString,
          disposeRequest.prConsent.toString,
          anonymize(disposeRequest.referenceNumber),
          anonymize(disposeRequest.registrationNumber)) ++
          disposeRequest.traderAddress.line.map(addr => anonymize(addr)) ++
          Seq(anonymize(disposeRequest.traderAddress.postTown),
            anonymize(disposeRequest.traderAddress.postCode),
            anonymize(disposeRequest.traderAddress.uprn),
            anonymize(disposeRequest.traderName),
            disposeRequest.transactionTimestamp
          )) )

      webService.invoke(disposeRequest, request.cookies.trackingId()).map {
        case (httpResponseCode, response) => {
          Some(Redirect(nextPage(httpResponseCode, response, disposeRequest))).
            map(_.withCookie(disposeFormModel)).
            map(storeResponseInCache(response, _)).
            map(transactionTimestamp).
            map(_.withCookie(PreventGoingToDisposePageCacheKey, "")). // US320 interstitial should redirect to DisposeSuccess.
            get
        }
      }.recover {
        case e: Throwable =>
          logMessage(request.cookies.trackingId(), Warn, s"Dispose micro-service call failed with exception ${e.getMessage}")
          onMicroserviceError
      }
    }

    def storeResponseInCache(response: Option[DisposeResponseDto], nextPage: Result): Result =
      response match {
        case Some(o) =>
          logMessage(request.cookies.trackingId(), Debug,"Dispose micro-service response",
            Some(Seq(o.auditId, anonymize(o.registrationNumber), o.responseCode.getOrElse(""), anonymize(o.transactionId))))

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
      val dateTime = disposeFormModel.dateOfDisposal.toDateTimeAtStartOfDay(DateTimeZone.forID("UTC"))
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

    def handleResponseCode(disposeResponseCode: String, transactionId: String,
                           disposeRequest: DisposeRequestDto)(implicit request: Request[_]): Call =
      disposeResponseCode match {
        case "ms.vehiclesService.response.unableToProcessApplication" =>
          logMessage(request.cookies.trackingId(), Warn,s"Dispose soap endpoint redirecting to dispose failure page." +
            s"Code returned from ms was $disposeResponseCode")
          onDisposeFailure
        case "ms.vehiclesService.response.duplicateDisposalToTrade" =>
          logMessage(request.cookies.trackingId(), Warn, s"Dispose soap endpoint redirecting to duplicate disposal page" +
            s"Code returned from ms was $disposeResponseCode")
          onDuplicateDispose
        case "X0001" | "W0075" =>
          logDisposeRequest(disposeResponseCode, disposeRequest)
          createAndSendEmailRequiringFurtherAction(transactionId, disposeRequest)
          onDisposeSuccess
        case _ =>
          logMessage(request.cookies.trackingId(), Warn, s"Dispose micro-service failed so now redirecting to micro service error page. " +
            s"Code returned from ms was $disposeResponseCode")
          microserviceErrorCall
      }

    def handleHttpStatusCode(statusCode: Int): Call =
      statusCode match {
        case OK => {
          logMessage(request.cookies.trackingId(), Debug, s"Dispose micro-service success so now redirecting to ${onDisposeSuccess}")
          onDisposeSuccess
        }
        case _ => {
          logMessage(request.cookies.trackingId(), Warn, s"Dispose micro-service failed so now redirecting to micro service error page. " +
            s"Code returned from ms was $statusCode")
          microserviceErrorCall
        }
      }

    (request.cookies.getModel[TraderDetailsModel], request.cookies.getModel[VehicleLookupFormModel]) match {
      case (Some(traderDetails), Some(vehicleLookup)) =>
        callMicroService(vehicleLookup, disposeFormModel, traderDetails)
      case _ => Future {
        logMessage(request.cookies.trackingId(), Error, s"Could not find either dealer details or VehicleLookupFormModel " +
          s"in cache on Dispose submit so redirect to ${onTraderDetailsMissing}")
        onTraderDetailsMissing
      }
    }
  }

  def logDisposeRequest(disposeResponseCode: String,
                        disposeRequest: DisposeRequestDto)(implicit request: Request[_]) = {
    logMessage( request.cookies.trackingId(), Error, disposeResponseCode,
      Some(Seq(disposeRequest.dateOfDisposal,
        disposeRequest.keeperConsent.toString,
        disposeRequest.mileage.toString,
        disposeRequest.prConsent.toString,
        anonymize(disposeRequest.referenceNumber),
        anonymize(disposeRequest.registrationNumber))++
        disposeRequest.traderAddress.line.map(addr => anonymize(addr)) ++
        Seq(anonymize(disposeRequest.traderAddress.postTown),
          anonymize(disposeRequest.traderAddress.postCode),
          anonymize(disposeRequest.traderAddress.uprn),
          anonymize(disposeRequest.traderName),
          disposeRequest.transactionTimestamp
        )) )
  }

  def createAndSendEmailRequiringFurtherAction(transactionId: String,
                                               disposeRequest: DisposeRequestDto)(implicit request: Request[_]) = {

    import SEND._ // Keep this local so that we don't pollute rest of the class with unnecessary imports.

    implicit val emailConfiguration = config.emailConfiguration
    implicit val implicitEmailService = implicitly[EmailService](emailService)

    val email = config.emailConfiguration.feedbackEmail.email

    val dateTime = DateTime.parse(disposeRequest.transactionTimestamp).toString("dd/MM/yy HH:mm")

    val message1 =
      s"""
         |Vehicle Registration:  ${disposeRequest.registrationNumber}
         |Transaction ID:  ${transactionId}
         |Date/Time of Transaction: ${dateTime}
      """.stripMargin

    val message2 =
      s"""
         |Trader Name:  ${disposeRequest.traderName}
         |Trader Address:  ${disposeRequest.traderAddress.line.mkString("\n                 ")}
         |                 ${disposeRequest.traderAddress.postTown.getOrElse("NOT ENTERED")}
         |                 ${disposeRequest.traderAddress.postCode}
         |Document Reference Number: ${disposeRequest.referenceNumber}
         |Mileage: ${disposeRequest.mileage.getOrElse("NOT ENTERED")}
         |Date of Sale:  ${DateTime.parse(disposeRequest.dateOfDisposal).toString("dd/MM/yy")}
         |Transaction ID:  ${transactionId}
         |Date/Time of Transaction:  ${dateTime}
      """.stripMargin

    SEND
      .email(Contents(message1, message1))
      .withSubject(s"Disposal Failure (1 of 2) ${transactionId}")
      .to(email)
      .send(request.cookies.trackingId)

    SEND
      .email(Contents(message2, message2))
      .withSubject(s"Disposal Failure (2 of 2) ${transactionId}")
      .to(email)
      .send(request.cookies.trackingId)
  }
}
