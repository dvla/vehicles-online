package controllers

import com.google.inject.Inject
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.DisposeFormModel.PreventGoingToDisposePageCacheKey
import play.api.mvc.Action
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import utils.helpers.Config
import uk.gov.dvla.vehicles.presentation.common.mappings.Time.fromMinutes

class MicroServiceError @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                  config: Config) extends BusinessController {

  protected val defaultRedirectUrl = controllers.routes.VehicleLookup.present().url
  protected val tryAgainTarget = controllers.routes.MicroServiceError.back()
  protected val exitTarget = controllers.routes.BeforeYouStart.present()

  def present = Action { implicit request =>
    val trackingId = request.cookies.trackingId()
    logMessage(trackingId, Debug, "Displaying MicroServiceError page")

    val referer = request.headers.get(REFERER) match {
      case Some(ref) => new java.net.URI(ref).getPath()
      case None => defaultRedirectUrl
    }

    logMessage(request.cookies.trackingId(), Debug, s"Referer $referer")
    logMessage(request.cookies.trackingId(), Debug, s"Try again target $tryAgainTarget")

    val unavailable = ServiceUnavailable(
      views.html.disposal_of_vehicle.micro_service_error(
        fromMinutes(config.openingTimeMinOfDay),
        fromMinutes(config.closingTimeMinOfDay),
        tryAgainTarget,
        exitTarget
      )
    )

    // Doesn't make sense to store this page as its own referer
    (if (request.path != referer)
      // Save the previous page URL (from the referrer header) into a cookie.
      unavailable.withCookie(MicroServiceError.MicroServiceErrorRefererCacheKey, referer)
    else
      unavailable
    )
      // Remove the interstitial cookie so we do not get bounced back to vehicle lookup unless we were on that page
      .discardingCookie(PreventGoingToDisposePageCacheKey)
  }

  def back = Action { implicit request =>
    val referer: String = request.cookies
      .getString(MicroServiceError.MicroServiceErrorRefererCacheKey)
      .getOrElse(defaultRedirectUrl)
    Redirect(referer).discardingCookie(MicroServiceError.MicroServiceErrorRefererCacheKey)
  }
}

object MicroServiceError {
  final val MicroServiceErrorRefererCacheKey = s"${CookiePrefix}msError"
}
