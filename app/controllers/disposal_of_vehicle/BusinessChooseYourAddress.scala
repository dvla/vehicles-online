package controllers.disposal_of_vehicle

import javax.inject.Inject

import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClientSideSessionFactory, ClientSideSession, CookieImplicits}
import CookieImplicits.{RichCookies, RichForm, RichSimpleResult}
import viewmodels.BusinessChooseYourAddressViewModel.Form.AddressSelectId
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.i18n.Lang
import play.api.mvc.{Action, Controller, Request}
import utils.helpers.Config
import utils.helpers.FormExtensions.formBinding
import viewmodels.EnterAddressManuallyViewModel.EnterAddressManuallyCacheKey
import viewmodels.{BusinessChooseYourAddressViewModel, SetupTradeDetailsViewModel, TraderDetailsViewModel}
import views.html.disposal_of_vehicle.business_choose_your_address
import webserviceclients.address_lookup.AddressLookupService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class BusinessChooseYourAddress @Inject()(addressLookupService: AddressLookupService)
                                               (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                config: Config) extends Controller {

  private[disposal_of_vehicle] val form = Form(BusinessChooseYourAddressViewModel.Form.Mapping)

  def present = Action.async { implicit request =>
    request.cookies.getModel[SetupTradeDetailsViewModel] match {
      case Some(setupTradeDetailsModel) =>
        val session = clientSideSessionFactory.getSession(request.cookies)
        fetchAddresses(setupTradeDetailsModel)(session, lang).map { addresses =>
          Ok(views.html.disposal_of_vehicle.business_choose_your_address(form.fill(),
            setupTradeDetailsModel.traderBusinessName,
            setupTradeDetailsModel.traderPostcode,
            addresses))
        }
      case None => Future {
        Redirect(routes.SetUpTradeDetails.present())
      }
    }
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        request.cookies.getModel[SetupTradeDetailsViewModel] match {
          case Some(setupTradeDetails) =>
            implicit val session = clientSideSessionFactory.getSession(request.cookies)
            fetchAddresses(setupTradeDetails).map { addresses =>
              BadRequest(business_choose_your_address(formWithReplacedErrors(invalidForm),
                setupTradeDetails.traderBusinessName,
                setupTradeDetails.traderPostcode,
                addresses))
            }
          case None => Future {
            Logger.error("Failed to find dealer details, redirecting")
            Redirect(routes.SetUpTradeDetails.present())
          }
        },
      validForm =>
        request.cookies.getModel[SetupTradeDetailsViewModel] match {
          case Some(setupTradeDetailsModel) =>
            implicit val session = clientSideSessionFactory.getSession(request.cookies)
            lookupUprn(validForm, setupTradeDetailsModel.traderBusinessName)
          case None => Future {
            Logger.error("Failed to find dealer details, redirecting")
            Redirect(routes.SetUpTradeDetails.present())
          }
        }
    )
  }

  private def formWithReplacedErrors(form: Form[BusinessChooseYourAddressViewModel])(implicit request: Request[_]) =
    form.replaceError(AddressSelectId, "error.required",
      FormError(key = AddressSelectId, message = "disposal_businessChooseYourAddress.address.required", args = Seq.empty)).
      distinctErrors

  private def fetchAddresses(model: SetupTradeDetailsViewModel)(implicit session: ClientSideSession, lang: Lang) =
    addressLookupService.fetchAddressesForPostcode(model.traderPostcode, session.trackingId)

  private def lookupUprn(model: BusinessChooseYourAddressViewModel, traderName: String)
                        (implicit request: Request[_], session: ClientSideSession) = {
    val lookedUpAddress = addressLookupService.fetchAddressForUprn(model.uprnSelected.toString, session.trackingId)
    lookedUpAddress.map {
      case Some(addressViewModel) =>
        val traderDetailsModel = TraderDetailsViewModel(traderName = traderName, traderAddress = addressViewModel)
        /* The redirect is done as the final step within the map so that:
         1) we are not blocking threads
         2) the browser does not change page before the future has completed and written to the cache. */
        Redirect(routes.VehicleLookup.present()).
          discardingCookie(EnterAddressManuallyCacheKey).
          withCookie(model).
          withCookie(traderDetailsModel)
      case None => Redirect(routes.UprnNotFound.present())
    }
  }
}
