package controllers

import com.google.inject.Inject
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.model.{TraderDetailsModel, BruteForcePreventionModel}
import utils.helpers.Config
import viewmodels.{AllCacheKeys, DisposeCacheKeys}

final class VrmLocked @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                  config: Config) extends Controller {

  def present = Action { implicit request =>
      request.cookies.getModel[BruteForcePreventionModel] match {
        case Some(viewModel) =>
          Logger.debug(s"VrmLocked - Displaying the vrm locked error page")
          Ok(views.html.disposal_of_vehicle.vrm_locked(viewModel.dateTimeISOChronology))
        case None =>
          Logger.debug("VrmLocked - Can't find cookie for BruteForcePreventionViewModel")
          Redirect(routes.VehicleLookup.present())
      }
  }

  def newDisposal = Action { implicit request =>
    request.cookies.getModel[TraderDetailsModel] match {
      case (Some(traderDetails)) =>
        Redirect(routes.VehicleLookup.present()).discardingCookies(DisposeCacheKeys)
      case _ => Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def exit = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present()).discardingCookies(AllCacheKeys)
  }
}
