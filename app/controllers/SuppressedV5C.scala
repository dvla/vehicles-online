package controllers

import com.google.inject.Inject
import play.api.mvc.Action
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import utils.helpers.Config

class SuppressedV5C @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                config: Config) extends BusinessController {

  protected val sellAnotherVehicleTarget = controllers.routes.SuppressedV5C.sellAnotherVehicle()
  protected val finishTarget = controllers.routes.SuppressedV5C.finish()
  protected val lookupAnotherVehicle = Redirect(routes.VehicleLookup.present())
  protected val onFinish = Redirect(routes.BeforeYouStart.present())
  
  def present = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Info, "Presenting suppressed V5C view")
    Ok(views.html.disposal_of_vehicle.suppressedV5C(sellAnotherVehicleTarget, finishTarget))
  }

  def sellAnotherVehicle = Action { implicit request =>
    lookupAnotherVehicle.discardingCookies(DisposeCacheKeys)
  }

  def finish = Action { implicit request =>
    onFinish.discardingCookies(AllCacheKeys)
  }
}
