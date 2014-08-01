package controllers

import com.google.inject.Inject
import models.AddressModel
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, Controller, Request}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichForm, RichCookies, RichSimpleResult}
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config
import viewmodels.{EnterAddressManuallyViewModel, SetupTradeDetailsViewModel, TraderDetailsViewModel}
import views.html.disposal_of_vehicle.enter_address_manually

final class EnterAddressManually @Inject()()
                                 (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                  config: Config) extends Controller {

  private[controllers] val form = Form(
    EnterAddressManuallyViewModel.FormMapping
  )

  def present = Action { implicit request =>
    request.cookies.getModel[SetupTradeDetailsViewModel] match {
      case Some(setupTradeDetails) =>
        Ok(enter_address_manually(form.fill(), traderPostcode = setupTradeDetails.traderPostcode))
      case None => Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        request.cookies.getModel[SetupTradeDetailsViewModel] match {
          case Some(setupTradeDetails) =>
            BadRequest(enter_address_manually(formWithReplacedErrors(invalidForm), setupTradeDetails.traderPostcode))
          case None =>
            Logger.debug("Failed to find dealer name in cache, redirecting")
            Redirect(routes.SetUpTradeDetails.present())
        },
      validForm =>
        request.cookies.getModel[SetupTradeDetailsViewModel] match {
          case Some(setupTradeDetails) =>
            val traderAddress = AddressModel.from(
              validForm.addressAndPostcodeModel,
              setupTradeDetails.traderPostcode
            )
            val traderDetailsModel = TraderDetailsViewModel(
              traderName = setupTradeDetails.traderBusinessName,
              traderAddress = traderAddress
            )

            Redirect(routes.VehicleLookup.present()).
              withCookie(validForm).
              withCookie(traderDetailsModel)
          case None =>
            Logger.debug("Failed to find dealer name in cache on submit, redirecting")
            Redirect(routes.SetUpTradeDetails.present())
        }
    )
  }

  private def formWithReplacedErrors(form: Form[EnterAddressManuallyViewModel])(implicit request: Request[_]) =
    form.replaceError(
      "addressAndPostcode.addressLines.buildingNameOrNumber",
      FormError("addressAndPostcode.addressLines", "error.address.buildingNameOrNumber.invalid")
    ).replaceError(
      "addressAndPostcode.addressLines.postTown",
      FormError("addressAndPostcode.addressLines",
      "error.address.postTown")
    ).replaceError(
      "addressAndPostcode.postcode",
      FormError("addressAndPostcode.postcode", "error.address.postcode.invalid")
    ).distinctErrors
}
