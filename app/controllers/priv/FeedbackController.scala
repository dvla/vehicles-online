package controllers.priv

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailService
import utils.helpers.Config

class FeedbackController @Inject()(emailService: EmailService)
                                  (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                   config: Config)
  extends controllers.FeedbackController(emailService) with PrivateKeeperController {

  protected override val formTarget = routes.FeedbackController.submit()
}