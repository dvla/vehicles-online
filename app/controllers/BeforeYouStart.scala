package controllers

import com.google.inject.Inject
import models.AllCacheKeys
import play.api.mvc.Action
import uk.gov.dvla.vehicles.presentation.common
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import utils.helpers.Config

class BeforeYouStart @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                 config: Config) extends BusinessController with DVLALogger {

  def present = Action { implicit request =>
    Ok(views.html.disposal_of_vehicle.before_you_start()).
      withNewSession.
      discardingCookies(AllCacheKeys)
  }

  def submit = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Debug,
      s"Redirecting from BeforeYouStart to ${routes.SetUpTradeDetails.present()}")
    Redirect(routes.SetUpTradeDetails.present())
  }
}
