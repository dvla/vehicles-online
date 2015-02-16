package controllers

import com.google.inject.Inject
import models.{AllCacheKeys, DisposeCacheKeys}
import play.api.mvc.{Action, Controller}
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichResult
import common.model.VehicleAndKeeperDetailsModel
import common.clientsidesession.CookieImplicits.RichCookies
import utils.helpers.Config

class SuppressedV5C @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                         config: Config) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.disposal_of_vehicle.suppressedV5C())
  }

  def sellAnotherVehicle = Action { implicit request =>
    Redirect(routes.VehicleLookup.present()).
      discardingCookies(DisposeCacheKeys)
  }

  def finish = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present()).
      discardingCookies(AllCacheKeys)
  }
}