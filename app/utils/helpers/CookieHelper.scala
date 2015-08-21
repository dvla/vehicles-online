package utils.helpers

import controllers.routes
import models.SeenCookieMessageCacheKey
import play.api.mvc.Results.Redirect
import play.api.mvc.{DiscardingCookie, RequestHeader, Result}

object CookieHelper {
  def discardAllCookies(implicit request: RequestHeader): Result = {
    val discardingCookiesKeys = request.cookies.map(_.name).filter(_ != SeenCookieMessageCacheKey)
    val discardingCookies = discardingCookiesKeys.map(DiscardingCookie(_)).toSeq
    Redirect(routes.BeforeYouStart.present())
      .discardingCookies(discardingCookies: _*)
  }
}
