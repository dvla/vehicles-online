package controllers

import com.google.inject.Inject
import models.{AllCacheKeys, DisposeCacheKeys, VrmLockedViewModel}
import models.DisposeCacheKeyPrefix.CookiePrefix
import org.joda.time.DateTime
import play.api.mvc.{Request, Result}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.controllers.VrmLockedBase
import uk.gov.dvla.vehicles.presentation.common.model.{BruteForcePreventionModel, TraderDetailsModel}
import utils.helpers.Config

class VrmLocked @Inject()()(implicit protected override val clientSideSessionFactory: ClientSideSessionFactory,
                                  config: Config) extends VrmLockedBase {

  protected val tryAnotherTarget = controllers.routes.VrmLocked.tryAnother()
  protected val exitTarget = controllers.routes.VrmLocked.exit()
  protected val bruteForceCookieMissing = Redirect(routes.VehicleLookup.present())
  protected val lookupAnotherVehicle = Redirect(routes.VehicleLookup.present())
  protected val onExit = Redirect(config.startUrl)

  protected override def presentResult(model: BruteForcePreventionModel)
                                      (implicit request: Request[_]): Result =
    Ok(views.html.disposal_of_vehicle.vrm_locked(
      VrmLockedViewModel(model.dateTimeISOChronology,
        DateTime.parse(model.dateTimeISOChronology).getMillis,
        tryAnotherTarget,
        exitTarget
      )
    ))

  protected override def missingBruteForcePreventionCookie(implicit request: Request[_]): Result =
    bruteForceCookieMissing

  protected override def tryAnotherResult(implicit request: Request[_]): Result =
    request.cookies.getModel[TraderDetailsModel] match {
      case (Some(traderDetails)) =>
        lookupAnotherVehicle.discardingCookies(DisposeCacheKeys)
      case _ => Redirect(routes.SetUpTradeDetails.present())
    }

  protected override def exitResult(implicit request: Request[_]): Result =
    onExit.discardingCookies(AllCacheKeys)
}
