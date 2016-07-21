package controllers.priv

import com.google.inject.Inject
import models.PrivateDisposeFormModel
import play.api.data.Form
import play.api.mvc.{Request, Result}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStats
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
    PrivateDisposeFormModel.Form.mapping(dateService)
  )

  override def fill(form: Form[PrivateDisposeFormModel])
          (implicit request: Request[_],
           clientSideSessionFactory: ClientSideSessionFactory): Form[PrivateDisposeFormModel] =
    form.fill()

  override def withModelCookie(result: Result, model: PrivateDisposeFormModel)
                    (implicit request: Request[_],
                     clientSideSessionFactory: ClientSideSessionFactory) =
    result.withCookie(model)

  override def onDisposeSuccessAction(transactionId: String, model: PrivateDisposeFormModel, traderEmail: Option[String])
                                     (implicit request: Request[_]) =
    createAndSendEmail(toTrader = false, isPrivate = true, transactionId, model.email)

  override protected val formTargetNoDateCheck = routes.Dispose.submitNoDateCheck()
  override protected val formTarget = routes.Dispose.submitWithDateCheck()
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

}
