package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClientSideSessionFactory, CookieImplicits}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import utils.helpers.Config
import viewmodels.AllCacheKeys

final class BeforeYouStart @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.disposal_of_vehicle.before_you_start()).
      withNewSession.
      discardingCookies(AllCacheKeys)
  }

  def submit = Action { implicit request =>
    Redirect(routes.SetUpTradeDetails.present())
  }
}
