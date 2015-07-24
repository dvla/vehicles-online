package controllers

import play.api.mvc.Controller

trait BusinessController extends Controller {
  protected implicit val isPrivateKeeper = false
}
