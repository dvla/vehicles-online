package controllers.priv

import controllers.BusinessController

trait PrivateKeeperController extends BusinessController {
  override protected implicit val isPrivateKeeper = true
}
