package controllers.priv

import com.google.inject.Inject
import email.EmailMessageBuilder
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.PrivateDisposeFormModel
import play.api.data.Form
import play.api.mvc.{Result, Request}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import common.clientsidesession.CookieImplicits.RichForm
import common.clientsidesession.CookieImplicits.RichResult
import common.model.VehicleAndKeeperDetailsModel
import common.services.DateService
import common.webserviceclients.emailservice.EmailService
import common.webserviceclients.healthstats.HealthStats
import utils.helpers.Config
import webserviceclients.dispose.DisposeService

class Dispose @Inject()(webService: DisposeService,
                        emailService: EmailService,
                        dateService: DateService,
                        healthStats: HealthStats)
                       (implicit clientSideSessionFactory: ClientSideSessionFactory,
                        config: Config) extends
  controllers.DisposeBase[PrivateDisposeFormModel](webService, emailService, dateService, healthStats) with PrivateKeeperController {

  def form = Form(
    models.PrivateDisposeFormModel.Form.mapping(dateService)
  )

  override def fill(form: Form[PrivateDisposeFormModel])
          (implicit request: Request[_],
           clientSideSessionFactory: ClientSideSessionFactory): Form[PrivateDisposeFormModel] =
    form.fill()

  override def withModelCookie(result: Result, model: PrivateDisposeFormModel)
                    (implicit request: Request[_],
                     clientSideSessionFactory: ClientSideSessionFactory) =
    result.withCookie(model)

  override def onDisposeSuccessAction(transactionId: String, model: PrivateDisposeFormModel)
                                     (implicit request: Request[_]) =
    createAndSendEmail(transactionId, model.email)

  override protected val formTarget = routes.Dispose.submit()
  override protected val backLink = routes.VehicleLookup.present()
  override protected val vehicleDetailsMissing = Redirect(routes.VehicleLookup.present())
  override protected val onVehicleAlreadyDisposed = Redirect(routes.VehicleLookup.present())
  override protected val onTraderDetailsMissing = Redirect(routes.SetUpTradeDetails.present())
  override protected val microserviceErrorCall = routes.MicroServiceError.present()
  override protected val onMicroserviceError = Redirect(routes.MicroServiceError.present())
  override protected val onDisposeFailure = routes.DisposeFailure.present()
  override protected val onDuplicateDispose = routes.DuplicateDisposalError.present()
  override protected val onDisposeSuccess = routes.DisposeSuccess.present()

  override protected val DisposeFormRegistrationNumberCacheKey =
    models.PrivateDisposeFormModel.DisposeFormRegistrationNumberCacheKey
  override protected val DisposeFormTimestampIdCacheKey =
    models.PrivateDisposeFormModel.DisposeFormTimestampIdCacheKey
  override protected val DisposeFormTransactionIdCacheKey =
    models.PrivateDisposeFormModel.DisposeFormTransactionIdCacheKey
  override protected val PreventGoingToDisposePageCacheKey =
    models.PrivateDisposeFormModel.PreventGoingToDisposePageCacheKey

  /**
   * Calling this method on a successful submission, will send an email if we have the new keeper details.
   */
  def createAndSendEmail(transactionId: String, email: Option[String])(implicit request: Request[_]) = {
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

        val template = EmailMessageBuilder.buildWith(vehicleDetails, transactionId, config.imagesPath)

        logMessage(request.cookies.trackingId(), Info, s"Sending email message via SEND service...")

        // This sends the email.
        val subject = s"$registrationNumber Confirmation of new vehicle keeper"
        SEND email template withSubject subject to emailAddr send request.cookies.trackingId

      case None => logMessage(request.cookies.trackingId(), Warn, s"Tried to send an email with no keeper details")
    }
  }
}