package controllers

import play.api.mvc.Controller
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger

trait BusinessController extends Controller with DVLALogger {
  protected implicit val isPrivateKeeper = false
}
