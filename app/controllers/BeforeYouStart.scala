package controllers

import com.google.inject.Inject
import models.AllCacheKeys
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.LogFormats.logMessage
import utils.helpers.Config

class BeforeYouStart @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                 config: Config) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.disposal_of_vehicle.before_you_start()).
      withNewSession.
      discardingCookies(AllCacheKeys)
  }

  def submit = Action { implicit request =>
    Logger.debug(logMessage(s"Redirecting from BeforeYouStart to ${routes.SetUpTradeDetails.present()}",
      request.cookies.trackingId()))
    Redirect(routes.SetUpTradeDetails.present())
  }
}
