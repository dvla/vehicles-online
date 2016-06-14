package controllers

import email.EmailMessageBuilder
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
import common.clientsidesession.{ClientSideSessionFactory, TrackingId}
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.LogFormats.anonymize
import common.model.{TraderDetailsModel, VehicleAndKeeperDetailsModel}
import common.services.{DateService, SEND}
import common.views.constraints.RegistrationNumber.formatVrm
import common.views.helpers.FormExtensions.RichForm
import common.webserviceclients.common.{VssWebEndUserDto, VssWebHeaderDto}
import common.webserviceclients.emailservice.EmailService
import common.webserviceclients.healthstats.HealthStats
import utils.helpers.Config
import views.html.disposal_of_vehicle.dispose
import webserviceclients.dispose.{DisposalAddressDto, DisposeRequestDto, DisposeResponseDto, DisposeService}

abstract class DisposeBase[FormModel <: DisposeFormModelBase]
  (webService: DisposeService, emailService: EmailService, dateService: DateService, healthStats: HealthStats)
  (implicit clientSideSessionFactory: ClientSideSessionFactory, config: Config) extends BusinessController {

  def onDisposeSuccessAction(transactionId: String, model: FormModel, traderEmail: Option[String])(implicit request: Request[_])
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
            val disposeViewModel = DisposeViewModel(vehicleDetails, traderDetails)
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
          val disposeViewModel = DisposeViewModel(vehicleDetails, traderDetails)
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

  private def nextPageOk(disposeRequest: DisposeRequestDto,
                         disposeResponse: Option[DisposeResponseDto],
                         disposeFormModel: FormModel,
                         traderEmail: Option[String])(implicit request: Request[AnyContent]): Call = {
    logMessage(request.cookies.trackingId(), Debug,
      s"Dispose micro-service returned success so now redirecting to $onDisposeSuccess")
    for {
      dr <- disposeResponse
      r <- dr.response
    } yield {
      r.message match {
        case "ms.vehiclesService.response.furtherActionRequired" =>
          val msg = s"Response code ${r.code} returned, which indicates a disposal failure " +
            "and emails need to be sent to the service feedback email address."
          logMessage(request.cookies.trackingId(), Error, msg, disposeRequestForLogging(disposeRequest))
          createAndSendEmailsRequiringFurtherAction(dr.disposeResponse.transactionId, disposeRequest)
        case _ =>
      }
    }
    // OK should always contain a disposeResponse
    onDisposeSuccessAction(disposeResponse.get.disposeResponse.transactionId, disposeFormModel, traderEmail)
    onDisposeSuccess
  }

  private def nextPageForbidden(statusCode: Int, disposeResponse: Option[DisposeResponseDto])
                               (implicit request: Request[AnyContent]): Call = {
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
  }

  private def nextPage(statusCode: Int,
                       disposeResponse: Option[DisposeResponseDto],
                       disposeRequest: DisposeRequestDto,
                       traderEmail: Option[String],
                       disposeFormModel: FormModel)(implicit request: Request[AnyContent]): Call =
    statusCode match {
      case OK =>
        nextPageOk(disposeRequest, disposeResponse, disposeFormModel, traderEmail)
      case FORBIDDEN =>
        nextPageForbidden(statusCode, disposeResponse)
      case _ =>
        logMessage(request.cookies.trackingId(), Warn,
          "Dispose micro-service failed so now redirecting to micro service error view. " +
            s"Code returned from ms was $statusCode")
        microserviceErrorCall
    }

  private def storeResponseInCache(response: Option[DisposeResponseDto], nextPage: Result)
                                  (implicit request: Request[AnyContent]): Result =
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
        // DE817 - Format the registration number for the user to view
          nextPageWithTransactionId.withCookie(
            DisposeFormRegistrationNumberCacheKey,
            formatVrm(o.disposeResponse.registrationNumber)
          )
        else nextPageWithTransactionId
      case None => nextPage
    }

  private def transactionTimestamp(nextPage: Result)(implicit request: Request[AnyContent]) = {
    val transactionTimestamp = dateService.today.toDateTime.get
    val formatter = ISODateTimeFormat.dateTime()
    val isoDateTimeString = formatter.print(transactionTimestamp)
    nextPage.withCookie(DisposeFormTimestampIdCacheKey, isoDateTimeString)
  }

  private def buildDisposeMicroServiceRequest(vehicleLookup: VehicleLookupFormModel,
                                      disposeFormModel: FormModel,
                                      traderDetails: TraderDetailsModel,
                                      trackingId: TrackingId,
                                      identifier: String): DisposeRequestDto = {
    val dateTime = disposeFormModel.dateOfDisposal.toDateTimeAtStartOfDay(DateTimeZone.UTC)
    val formatter = ISODateTimeFormat.dateTime()
    val isoDateTimeString = formatter.print(dateTime)

    DisposeRequestDto(buildWebHeader(trackingId, identifier),
      referenceNumber = vehicleLookup.referenceNumber,
      registrationNumber = vehicleLookup.registrationNumber,
      traderName = traderDetails.traderName,
      traderAddress = DisposalAddressDto.from(traderDetails.traderAddress),
      dateOfDisposal = isoDateTimeString,
      transactionTimestamp = formatter.print(dateService.now.toDateTime),
      prConsent = disposeFormModel.lossOfRegistrationConsent.toBoolean,
      keeperConsent = disposeFormModel.consent.toBoolean,
      mileage = disposeFormModel.mileage
    )
  }

  private def buildWebHeader(trackingId: TrackingId, identifier: String): VssWebHeaderDto = {

    def buildEndUser(identifier: String): VssWebEndUserDto =
      VssWebEndUserDto(endUserId = identifier, orgBusUnit = config.orgBusinessUnit)

    VssWebHeaderDto(transactionId = trackingId.value,
      originDateTime = dateService.now.toDateTime,
      applicationCode = config.applicationCode,
      serviceTypeCode = config.vssServiceTypeCode,
      buildEndUser(identifier))
  }

  private def doDisposeAction(webService: DisposeService, disposeFormModel: FormModel)
                             (implicit request: Request[AnyContent]): Future[Result] = {

    def callMicroService(vehicleLookup: VehicleLookupFormModel,
                         disposeFormModel: FormModel,
                         traderDetails: TraderDetailsModel) = {
      val disposeRequest = buildDisposeMicroServiceRequest(vehicleLookup, disposeFormModel, traderDetails,
        request.cookies.trackingId(),
        request.cookies.getString(models.IdentifierCacheKey).getOrElse(traderDetails.traderName)
      )

      logMessage(request.cookies.trackingId(), Debug, "Calling dispose micro-service with request: ",
        disposeRequestForLogging(disposeRequest))
      webService.invoke(disposeRequest, request.cookies.trackingId()).map {
        case (httpResponseCode, response) =>
          Some(Redirect(nextPage(httpResponseCode, response, disposeRequest, traderDetails.traderEmail, disposeFormModel)))
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

  private def disposeRequestForLogging(disposeRequest: DisposeRequestDto)
                                      (implicit request: Request[_]): Option[Seq[String]] = {
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
        )
    )
  }

  private def createAndSendEmailsRequiringFurtherAction(transactionId: String,
                                               disposeRequest: DisposeRequestDto)(implicit request: Request[_]) = {

    import SEND.Contents // Keep this local so that we don't pollute rest of the class with unnecessary imports.
    val emailAddress = config.emailConfiguration.feedbackEmail.email
    implicit val emailConfiguration = config.emailConfiguration
    implicit val implicitEmailService = implicitly[EmailService](emailService)
    implicit val implicitDateService = implicitly[DateService](dateService)
    implicit val implicitHealthStats = implicitly[HealthStats](healthStats)

    val emailHelper = EmailHelper(transactionId, disposeRequest)

    SEND.email(Contents(emailHelper.message1Html, emailHelper.message1))
      .withSubject(emailHelper.message1Title)
      .to(emailAddress)
      .send(request.cookies.trackingId)

    SEND.email(Contents(emailHelper.message2Html, emailHelper.message2))
      .withSubject(emailHelper.message2Title)
      .to(emailAddress)
      .send(request.cookies.trackingId)
  }

  /**
   * Calling this method on a successful submission, will send an email. Called by sub-classes.
   */
  protected def createAndSendEmail(toTrader: Boolean, isPrivate: Boolean, transactionId: String, email: Option[String])
                                  (implicit request: Request[_]) = {
    email match {
      case Some(emailAddr) =>
        import scala.language.postfixOps
        import common.services.SEND

        implicit val emailConfiguration = config.emailConfiguration
        implicit val implicitEmailService = implicitly[EmailService](emailService)
        implicit val implicitDateService = implicitly[DateService](dateService)
        implicit val implicitHealthStats = implicitly[HealthStats](healthStats)

        val vehicleDetails = request.cookies.getModel[VehicleAndKeeperDetailsModel]

        val registrationNumber = vehicleDetails.map(_.registrationNumber).getOrElse("")

        val template = EmailMessageBuilder.buildWith(vehicleDetails,
          transactionId,
          config.imagesPath,
          new DateTime,
          isPrivate,
          toTrader)

        logMessage(request.cookies.trackingId(), Info, s"Sending email message via SEND service...")

        // This sends the email.
        var subjectDetail = s"Confirmation of new vehicle keeper"
        if (!isPrivate)
          subjectDetail = s"Confirmation of selling to motor trade"
        val subject = s"$registrationNumber " + subjectDetail
        SEND email template withSubject subject to emailAddr send request.cookies.trackingId

      case None => logMessage(request.cookies.trackingId(), Warn, s"Tried to send an email with no email address")
    }
  }
}

private case class EmailHelper(transactionId: String, disposeRequest: DisposeRequestDto) {
  private val htmlTemplateStart = (title: String) =>
    s"""
       |<!DOCTYPE html>
       |<head>
       |<title>$title</title>
       |</head>
       |<body>
       |<ul style="padding: 0; list-style-type: none;">
       """.stripMargin

  private val htmlTemplateEnd =
    s"""
       |</ul>
       |</body>
       |</html>
      """.stripMargin

  val message1Title = s"Disposal Failure (1 of 2) $transactionId"

  private val dateTime = (disposeRequest: DisposeRequestDto) => DateTime.parse(disposeRequest.transactionTimestamp).toString("dd/MM/yy HH:mm")

  private val message1Template = (start: (String) => String, end: String, startLine: String, endLine: String) =>
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

  private val message2Template = (start: (String) => String, end: String, startLine: String, endLine: String,
                          addressSep: String, addressPad: String) =>
    start(message2Title) +
      s"""
         |${startLine}Trader Name:  ${disposeRequest.traderName}$endLine
         |${startLine}Trader Address:  ${disposeRequest.traderAddress.line.mkString(addressSep + addressPad)}$endLine
         |$addressPad${disposeRequest.traderAddress.postTown.getOrElse("Not entered")}$endLine
         |$addressPad${disposeRequest.traderAddress.postCode}$endLine
         |${startLine}Document Reference Number: ${disposeRequest.referenceNumber}$endLine
         |${startLine}Mileage: ${disposeRequest.mileage.getOrElse("Not entered")}$endLine
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
}