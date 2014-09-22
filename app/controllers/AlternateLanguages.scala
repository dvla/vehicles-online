package controllers

import play.api.Play.current
import play.api.i18n.Lang
import play.api.mvc.{Action, Controller}

object AlternateLanguages extends Controller {
  final val CyId = "cy"
  final val EnId = "en"
  val langCy = Lang(CyId)
  val langEn = Lang(EnId)

  def withLanguage(chosenLanguage: String) = Action { implicit request =>
    Redirect(request.headers.get(REFERER).getOrElse("No Referer in header")).
      withLang(Lang(chosenLanguage))
  }
}
