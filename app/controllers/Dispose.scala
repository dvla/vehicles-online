package controllers

import com.google.inject.Inject
import models.DisposeFormModel
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
                       (implicit clientSideSessionFactory: ClientSideSessionFactory, config: Config)
  extends controllers.DisposeBase[DisposeFormModel](webService, emailService, dateService, healthStats) with BusinessController {

  def form = Form(
    DisposeFormModel.Form.mapping(dateService)
  )

  override def fill(form: Form[DisposeFormModel])
          (implicit request: Request[_],
          clientSideSessionFactory: ClientSideSessionFactory): Form[DisposeFormModel] =
    form.fill()

  override def withModelCookie(result: Result, model: DisposeFormModel)
                    (implicit request: Request[_],
                     clientSideSessionFactory: ClientSideSessionFactory) =
    result.withCookie(model)

  override def onDisposeSuccessAction(transactionId: String, model: DisposeFormModel, traderEmail: Option[String])(implicit request: Request[_]) = {
    createAndSendEmail(false, false, transactionId, model.email) // email seller
    createAndSendEmail(true, false, transactionId, traderEmail) // email trader
  }

}
