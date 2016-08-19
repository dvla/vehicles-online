package controllers

import com.google.inject.Inject
import play.api.mvc.Action
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.model.CookieReport
import utils.helpers.Config

class CookiePolicy @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory, config: Config)
                             extends uk.gov.dvla.vehicles.presentation.common.controllers.CookiePolicy
                             with BusinessController {

  private val reports = cookies ++ List(
    CookieReport("tracking_id", "tracking_id", "session", "close"),
    CookieReport("PLAY_LANG", "PLAY_LANG", "session", "close"),
    CookieReport("40 character length", "encrypted", "session-secure", "close"),
    CookieReport("40 character length", "multi-encrypted", "normal-secure", "8hours")
  )

  def present = Action { implicit request =>
    Ok(views.html.disposal_of_vehicle.cookie_policy(reports))
  }
}
