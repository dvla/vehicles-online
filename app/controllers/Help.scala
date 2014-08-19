package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import utils.helpers.Config
import viewmodels.HelpCacheKey

final class Help @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                             config: Config) extends Controller {

  def present = Action { implicit request =>
    val origin = request.headers.get(REFERER).getOrElse("No Referer in header")
    Ok(views.html.common.help()).
      withCookie(HelpCacheKey, origin) // Save the previous page URL (from the referer header) into a cookie.
  }

  def back = Action { implicit request =>
    val origin: String = request.cookies.getString(HelpCacheKey).getOrElse(routes.BeforeYouStart.present().url)
    Redirect(origin).
      discardingCookie(HelpCacheKey)
  }
}