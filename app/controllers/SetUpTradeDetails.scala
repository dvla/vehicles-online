package controllers

import com.google.inject.Inject
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichForm, RichResult}
import utils.helpers.Config
import models.SetupTradeDetailsFormModel
import models.SetupTradeDetailsFormModel.Form.{TraderNameId, TraderPostcodeId}
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding

final class SetUpTradeDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                          config: Config) extends Controller {

  private[controllers] val form = Form(
    SetupTradeDetailsFormModel.Form.Mapping
  )

  def present = Action { implicit request =>
    Ok(views.html.disposal_of_vehicle.setup_trade_details(form.fill()))
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => {
        val formWithReplacedErrors = invalidForm.replaceError(
          TraderNameId,
          FormError(key = TraderNameId, message = "error.validBusinessName", args = Seq.empty)
        ).replaceError(
          TraderPostcodeId,
          FormError(key = TraderPostcodeId, message = "error.restricted.validPostcode", args = Seq.empty)
        ).distinctErrors
        BadRequest(views.html.disposal_of_vehicle.setup_trade_details(formWithReplacedErrors))
      },
      validForm => Redirect(routes.BusinessChooseYourAddress.present()).withCookie(validForm)
    )
  }
}
