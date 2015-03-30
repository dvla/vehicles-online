package controllers

import com.google.inject.Inject
import models.HelpCacheKey
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import utils.helpers.Config

class Help @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                       config: Config) extends Controller {

  protected val backTarget = controllers.routes.Help.back()
  protected val exitTarget = controllers.routes.BeforeYouStart.present()

  def present = Action { implicit request =>
    val origin = request.headers.get(REFERER).getOrElse("No Referer in header")
    Ok(views.html.common.help(backTarget, exitTarget)).
      withCookie(HelpCacheKey, origin) // Save the previous page URL (from the referer header) into a cookie.
  }

  def back = Action { implicit request =>
    val origin: String = request.cookies.getString(HelpCacheKey).getOrElse(config.startUrl)
    Redirect(origin).discardingCookie(HelpCacheKey)
  }
}
