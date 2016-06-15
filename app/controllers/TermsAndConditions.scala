package controllers

import com.google.inject.Inject
import play.api.mvc.Action
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class TermsAndConditions @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                   config: Config) extends BusinessController {

  def present = Action { implicit request =>
    Ok(views.html.disposal_of_vehicle.terms_and_conditions())
  }
}
