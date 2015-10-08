package controllers

import models.DisposeCacheKeyPrefix.CookiePrefix
import models.DisposeFormModelBase
import models.DisposeFormModelBase.Form.ConsentId
import models.DisposeFormModelBase.Form.LossOfRegistrationConsentId
import models.DisposeFormModelBase.Form.MileageId
import models.DisposeViewModel
import models.VehicleLookupFormModel
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, AnyContent, Call, Request, Result}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.LogFormats.anonymize
import common.model.{TraderDetailsModel, VehicleAndKeeperDetailsModel}
import common.services.{DateService, SEND}
import common.views.helpers.FormExtensions.RichForm
import common.webserviceclients.emailservice.EmailService
import utils.helpers.Config
import views.html.disposal_of_vehicle.dispose
import webserviceclients.dispose.{DisposalAddressDto, DisposeRequestDto, DisposeResponseDto, DisposeService}

abstract class DisposeBase[FormModel <: DisposeFormModelBase]
  (webService: DisposeService, emailService: EmailService, dateService: DateService)
  (implicit clientSideSessionFactory: ClientSideSessionFactory, config: Config) extends BusinessController {

  def onDisposeSuccessAction(transactionId: String, model: FormModel)(implicit request: Request[_])
  def form: Form[FormModel]
  def fill(form: Form[FormModel])
          (implicit request: Request[_], clientSideSessionFactory: ClientSideSessionFactory): Form[FormModel]
  def withModelCookie(result: Result, model: FormModel)
                    (implicit request: Request[_], clientSideSessionFactory: ClientSideSessionFactory): Result

  // Default to trader routing
  protected val formTarget = routes.Dispose.submit()
  protected val backLink = routes.VehicleLookup.present()
  protected val vehicleDetailsMissing = Redirect(routes.VehicleLookup.present())
  protected val onVehicleAlreadyDisposed = Redirect(routes.VehicleLookup.present())
  protected val onTraderDetailsMissing = Redirect(routes.SetUpTradeDetails.present())
  protected val microserviceErrorCall = routes.MicroServiceError.present()
  protected val onMicroserviceError = Redirect(routes.MicroServiceError.present())
  protected val onDisposeFailure = routes.DisposeFailure.present()
  protected val onDuplicateDispose = routes.DuplicateDisposalError.present()
  protected val onDisposeSuccess = routes.DisposeSuccess.present()

  // Default to trader cookies
  protected val DisposeFormRegistrationNumberCacheKey = models.DisposeFormModel.DisposeFormRegistrationNumberCacheKey
  protected val DisposeFormTimestampIdCacheKey = models.DisposeFormModel.DisposeFormTimestampIdCacheKey
  protected val DisposeFormTransactionIdCacheKey = models.DisposeFormModel.DisposeFormTransactionIdCacheKey
  protected val PreventGoingToDisposePageCacheKey = models.DisposeFormModel.PreventGoingToDisposePageCacheKey

  def present = Action { implicit request =>
    (request.cookies.getModel[TraderDetailsModel], request.cookies.getString(PreventGoingToDisposePageCacheKey)) match {
      case (Some(traderDetails), None) =>
        request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
          case (Some(vehicleDetails)) =>
            logMessage(request.cookies.trackingId(), Info, "Presenting dispose (complete and confirm) view")
            val disposeViewModel = createViewModel(traderDetails, vehicleDetails)
            Ok(dispose(disposeViewModel, fill(form), dateService, formTarget, backLink))
          case _ =>
            logMessage(request.cookies.trackingId(), Error,
              s"Failed to find vehicle details, redirecting to $vehicleDetailsMissing")
            vehicleDetailsMissing
        }
      case (_, Some(interstitial)) =>
        // US320 Kick them back to the VehicleLookup page if they arrive here by any route other that clicking the
        // "Exit" or "New Dispose" buttons.
        logMessage(request.cookies.trackingId(), Error,
          s"Vehicle ids already dispose, redirecting to $onVehicleAlreadyDisposed")
        onVehicleAlreadyDisposed.
          discardingCookie(PreventGoingToDisposePageCacheKey).
          discardingCookies(DisposeCacheKeys)
      case _ =>
        logMessage(request.cookies.trackingId(), Error,
          s"Failed to find dealer details, redirecting to $onTraderDetailsMissing")
        onTraderDetailsMissing
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
          logMessage(request.cookies.trackingId(), Error,
            "Could not find expected data in cache on dispose submit - now redirecting...")
          Redirect(routes.SetUpTradeDetails.present())
        }
      },
      validForm => {
        request.cookies.getString(PreventGoingToDisposePageCacheKey) match {
          case Some(_) =>
            logMessage(request.cookies.trackingId(), Error,
              s"Vehicle is already disposed, redirecting to $onVehicleAlreadyDisposed")
            Future.successful(onVehicleAlreadyDisposed)
          case None =>
            // US320 prevent user using the browser back button and resubmitting.
            doDisposeAction(webService, validForm)
        }
      }
    )
  }

  private def formWithReplacedErrors(form: Form[FormModel])(implicit request: Request[_]) = {
    // When the user doesn't select a value from the drop-down then the mapping will fail to match on an Int before
    // it gets to the constraints, so we need to replace the error type with one that will give a relevant message.
    val dateOfDisposalError = FormError("dateOfDisposal", "error.dateOfDisposal")
    import scala.language.implicitConversions
    implicit def toRichForm(form: Form[FormModel]): RichForm[FormModel] = new RichForm[FormModel](form)

    form
      .replaceError("dateOfDisposal.day", dateOfDisposalError)
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

  private def doDisposeAction(webService: DisposeService, disposeFormModel: FormModel)
                                (implicit request: Request[AnyContent]): Future[Result] = {

    def callMicroService(vehicleLookup: VehicleLookupFormModel,
                         disposeForm: FormModel,
                         traderDetails: TraderDetailsModel) = {
      val disposeRequest = buildDisposeMicroServiceRequest(vehicleLookup, disposeForm, traderDetails)
      logMessage(request.cookies.trackingId(), Info, "Calling Dispose micro-service")

      logMessage(request.cookies.trackingId(), Debug, "Dispose micro-service request",
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
          ))
      )

      webService.invoke(disposeRequest, request.cookies.trackingId()).map {
        case (httpResponseCode, response) =>
          Some(Redirect(nextPage(httpResponseCode, response, disposeRequest)))
            .map(withModelCookie(_, disposeFormModel))
            .map(storeResponseInCache(response, _))
            .map(transactionTimestamp)
            // Interstitial should redirect to DisposeSuccess
            .map(result => result.withCookie(PreventGoingToDisposePageCacheKey, ""))
            .get
      }.recover {
        case e: Throwable =>
          logMessage(request.cookies.trackingId(), Warn,
            s"Dispose micro-service call failed with exception ${e.getMessage}")
          onMicroserviceError
      }
    }

    def storeResponseInCache(response: Option[DisposeResponseDto], nextPage: Result): Result =
      response match {
        case Some(o) =>
          logMessage(request.cookies.trackingId(), Debug, "Dispose micro-service response", Some(
            Seq(o.disposeResponse.auditId, anonymize(o.disposeResponse.registrationNumber),
              o.response match { case Some(c) => c.code; case _ => "" }, anonymize(o.disposeResponse.transactionId))
          ))

          val nextPageWithTransactionId =
            if (!o.disposeResponse.transactionId.isEmpty)
              nextPage.withCookie(DisposeFormTransactionIdCacheKey, o.disposeResponse.transactionId)
            else nextPage

          if (!o.disposeResponse.registrationNumber.isEmpty)
            nextPageWithTransactionId.withCookie(
              DisposeFormRegistrationNumberCacheKey,
              o.disposeResponse.registrationNumber
            )
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
                                        disposeForm: FormModel,
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

    def nextPage(statusCode: Int,
                 disposeResponse: Option[DisposeResponseDto],
                 disposeRequest: DisposeRequestDto): Call =
      statusCode match {
        case OK =>
          logMessage(request.cookies.trackingId(), Debug,
            s"Dispose micro-service returned success so now redirecting to $onDisposeSuccess")
          for {
            dr <- disposeResponse
            r <- dr.response
          } yield {
            r.message match {
              case "ms.vehiclesService.response.furtherActionRequired" =>
                logDisposeRequest(r.code, disposeRequest)
                createAndSendEmailRequiringFurtherAction(dr.disposeResponse.transactionId, disposeRequest)
              case _ =>
            }
          }
          // OK should always contain a disposeResponse
          onDisposeSuccessAction(disposeResponse.get.disposeResponse.transactionId, disposeFormModel)
          onDisposeSuccess
        case INTERNAL_SERVER_ERROR =>
          val call = for {
            dr <- disposeResponse
            r <- dr.response
          } yield {
              r.message match {
                case "ms.vehiclesService.response.unableToProcessApplication" =>
                  logMessage(request.cookies.trackingId(), Warn,
                    "Disposal of vehicle failed so now redirecting to dispose failure view. " +
                      s"HTTP code returned from ms was $statusCode with code ${r.message}")
                  onDisposeFailure
                case "ms.vehiclesService.response.duplicateDisposalToTrade" =>
                  logMessage(request.cookies.trackingId(), Warn,
                    "Disposal of vehicle failed so now redirecting to duplicate disposal view. " +
                      s"HTTP code returned from ms was $statusCode with code ${r.message}")
                  onDuplicateDispose
                case _ =>
                  logMessage(request.cookies.trackingId(), Warn,
                    "Dispose micro-service failed so now redirecting to micro service error view. " +
                      s"HTTP code returned from ms was $statusCode with code ${r.message}")
                  microserviceErrorCall
              }
            }
          call.getOrElse(microserviceErrorCall)
        case _ =>
          logMessage(request.cookies.trackingId(), Warn,
            "Dispose micro-service failed so now redirecting to micro service error view. " +
              s"Code returned from ms was $statusCode")
          microserviceErrorCall
      }

    (request.cookies.getModel[TraderDetailsModel], request.cookies.getModel[VehicleLookupFormModel]) match {
      case (Some(traderDetails), Some(vehicleLookup)) =>
        callMicroService(vehicleLookup, disposeFormModel, traderDetails)
      case _ => Future {
        logMessage(request.cookies.trackingId(), Error,
          "Could not find either dealer details or VehicleLookupFormModel " +
          s"in cache on Dispose submit so redirecting to $onTraderDetailsMissing")
        onTraderDetailsMissing
      }
    }
  }

  def logDisposeRequest(disposeResponseCode: String,
                        disposeRequest: DisposeRequestDto)(implicit request: Request[_]) = {
    logMessage(request.cookies.trackingId(), Error, disposeResponseCode,
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
        ))
    )
  }

  def createAndSendEmailRequiringFurtherAction(transactionId: String,
                                               disposeRequest: DisposeRequestDto)(implicit request: Request[_]) = {

    import SEND.Contents // Keep this local so that we don't pollute rest of the class with unnecessary imports.

    implicit val emailConfiguration = config.emailConfiguration
    implicit val implicitEmailService = implicitly[EmailService](emailService)

    val email = config.emailConfiguration.feedbackEmail.email

    val dateTime = DateTime.parse(disposeRequest.transactionTimestamp).toString("dd/MM/yy HH:mm")

    val htmlTemplateStart = (title: String) =>
      s"""
         |<!DOCTYPE html>
         |<head>
         |<title>$title</title>
         |</head>
         |<body>
         |<ul style="padding: 0; list-style-type: none;">
       """.stripMargin

    val htmlTemplateEnd =
      s"""
        |</ul>
        |</body>
        |</html>
      """.stripMargin

    val message1Title = s"Disposal Failure (1 of 2) $transactionId"

    val message1Template = (start: (String) => String, end: String, startLine: String, endLine: String) =>
      start(message1Title) +
      s"""
          |${startLine}Vehicle Registration:  ${disposeRequest.registrationNumber}$endLine
          |${startLine}Transaction ID:  $transactionId$endLine
          |${startLine}Date/Time of Transaction: $dateTime$endLine
      """.stripMargin +
      end

    val message1 = message1Template((_) => "", "", "", "")
    val message1Html = message1Template(htmlTemplateStart, htmlTemplateEnd, "<li>", "</li>")
    val message2Title = s"Disposal Failure (2 of 2) $transactionId"
    val message2Template = (start: (String) => String, end: String, startLine: String, endLine: String,
                            addressSep: String, addressPad: String) =>
      start(message2Title) +
      s"""
          |${startLine}Trader Name:  ${disposeRequest.traderName}$endLine
          |${startLine}Trader Address:  ${disposeRequest.traderAddress.line.mkString(addressSep + addressPad)}$endLine
          |$addressPad${disposeRequest.traderAddress.postTown.getOrElse("NOT ENTERED")}$endLine
          |$addressPad${disposeRequest.traderAddress.postCode}$endLine
          |${startLine}Document Reference Number: ${disposeRequest.referenceNumber}$endLine
          |${startLine}Mileage: ${disposeRequest.mileage.getOrElse("NOT ENTERED")}$endLine
          |${startLine}Date of Sale:  ${DateTime.parse(disposeRequest.dateOfDisposal).toString("dd/MM/yy")}$endLine
          |${startLine}Transaction ID:  $transactionId$endLine
          |${startLine}Date/Time of Transaction:  $dateTime$endLine
      """.stripMargin +
      end

    val message2 = message2Template((_) => "", "", "", "", "\n", "                 ")
    val message2Html = message2Template(
      htmlTemplateStart,
      htmlTemplateEnd,
      "<li>",
      "</li>",
      "</li>",
      "<li style='padding-left: 8.5em'>"
    )

    SEND
      .email(Contents(message1Html, message1))
      .withSubject(message1Title)
      .to(email)
      .send(request.cookies.trackingId)

    SEND
      .email(Contents(message2Html, message2))
      .withSubject(message2Title)
      .to(email)
      .send(request.cookies.trackingId)
  }
}
