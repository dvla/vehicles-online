package controllers

import com.google.inject.Inject
import controllers.routes.VehicleLookup
import models.DisposeFormModel.PreventGoingToDisposePageCacheKey
import models.CacheKeyPrefix
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import utils.helpers.Config

class MicroServiceError @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                        config: Config) extends Controller {
  private final val DefaultRedirectUrl = VehicleLookup.present().url

  def present = Action { implicit request =>
    val referer = request.headers.get(REFERER).getOrElse(DefaultRedirectUrl)
    ServiceUnavailable(views.html.disposal_of_vehicle.micro_service_error()).
      // Save the previous page URL (from the referer header) into a cookie.
      withCookie(MicroServiceError.MicroServiceErrorRefererCacheKey, referer).
      // Remove the interstitial cookie so we do not get bounced back to vehicle lookup unless we were on that page
      discardingCookie(PreventGoingToDisposePageCacheKey)
  }

  def back = Action { implicit request =>
    val referer: String = request.cookies.getString(MicroServiceError.MicroServiceErrorRefererCacheKey).getOrElse(DefaultRedirectUrl)
    Redirect(referer).discardingCookie(MicroServiceError.MicroServiceErrorRefererCacheKey)
  }
}

object MicroServiceError {
  final val MicroServiceErrorRefererCacheKey = s"${CacheKeyPrefix}msError"
}
