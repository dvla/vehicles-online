package controllers.priv

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config
import webserviceclients.emailservice.EmailService

class FeedbackController @Inject()(emailService: EmailService)(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                               config: Config)
  extends controllers.FeedbackController(emailService) {

  protected override val formTarget = routes.FeedbackController.submit()
}
