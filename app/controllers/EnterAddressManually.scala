package controllers

import com.google.inject.Inject
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.EnterAddressManuallyFormModel
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, Request}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.model.{VmAddressModel, TraderDetailsModel, SetupTradeDetailsFormModel}
import common.views.helpers.FormExtensions.formBinding
import uk.gov.dvla.vehicles.presentation.common.views.models.{AddressAndPostcodeViewModel, AddressLinesViewModel}
import utils.helpers.Config
import views.html.disposal_of_vehicle.enter_address_manually

class EnterAddressManually @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends BusinessController {

  private[controllers] val form = Form(
    EnterAddressManuallyFormModel.Form.Mapping
  )

  protected val formTarget = controllers.routes.EnterAddressManually.submit()
  protected val backLink = controllers.routes.BusinessChooseYourAddress.present()
  protected val onCookiesMissing = Redirect(routes.SetUpTradeDetails.present())
  protected val onSubmitSuccess = Redirect(routes.VehicleLookup.present())

  def present = Action { implicit request =>
    request.cookies.getModel[SetupTradeDetailsFormModel] match {
      case Some(setupTradeDetails) =>
        logMessage(request.cookies.trackingId(), Info, "Presenting enter address manually view")
        val formFill = request.cookies.getModel[EnterAddressManuallyFormModel] match {
          case Some(formModel) => form.fill(formModel)
          case None => form.fill(
            EnterAddressManuallyFormModel(
              AddressAndPostcodeViewModel(
                None,
                AddressLinesViewModel("", None, None, None, ""),
                setupTradeDetails.traderPostcode
              )
            )
          )
        }
        Ok(enter_address_manually(formFill, formTarget, backLink))
      case None => onCookiesMissing
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        request.cookies.getModel[SetupTradeDetailsFormModel] match {
          case Some(setupTradeDetails) =>
            BadRequest(enter_address_manually(formWithReplacedErrors(invalidForm), formTarget, backLink))
          case None =>
            logMessage(request.cookies.trackingId(), Debug,
              s"Failed to find dealer name in cache, redirecting to $onCookiesMissing")
            onCookiesMissing
        },
      validForm =>
        request.cookies.getModel[SetupTradeDetailsFormModel] match {
          case Some(setupTradeDetails) =>
            val traderAddress = VmAddressModel.from(validForm.addressAndPostcodeModel)
            val traderDetailsModel = TraderDetailsModel(
              traderName = setupTradeDetails.traderBusinessName,
              traderAddress = traderAddress
            )
            logMessage(request.cookies.trackingId(), Debug,
              s"Address found, redirecting to $onSubmitSuccess ")
            onSubmitSuccess.
              withCookie(validForm).
              withCookie(traderDetailsModel)
          case None =>
            logMessage(request.cookies.trackingId(), Debug,
              "Failed to find dealer name in cache on submit, " +
              s"redirecting to $onCookiesMissing")
            onCookiesMissing
        }
    )
  }

  private def formWithReplacedErrors(form: Form[EnterAddressManuallyFormModel])(implicit request: Request[_]) =
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
