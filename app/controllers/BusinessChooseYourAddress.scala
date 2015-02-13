package controllers

import javax.inject.Inject
import models.BusinessChooseYourAddressFormModel.Form.AddressSelectId
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import uk.gov.dvla.vehicles.presentation.common
import common.model.SetupTradeDetailsFormModel
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.clientsidesession.{ClientSideSession, ClientSideSessionFactory}
import common.model.{AddressModel, TraderDetailsModel}
import common.views.helpers.FormExtensions.formBinding
import common.webserviceclients.addresslookup.AddressLookupService
import models.BusinessChooseYourAddressFormModel
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.i18n.Lang
import play.api.mvc.{Action, Controller, Request, Result}
import utils.helpers.Config
import views.html.disposal_of_vehicle.business_choose_your_address
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import models.DisposeCacheKeyPrefix.CookiePrefix

class BusinessChooseYourAddress @Inject()(addressLookupService: AddressLookupService)
                                               (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                config: Config) extends Controller {

  private[controllers] val form = Form(BusinessChooseYourAddressFormModel.Form.Mapping)

  def present = Action.async { implicit request =>
    request.cookies.getModel[SetupTradeDetailsFormModel] match {
      case Some(setupTradeDetailsModel) =>
        val session = clientSideSessionFactory.getSession(request.cookies)
        fetchAddresses(setupTradeDetailsModel, showBusinessName = Some(true))(session, request2lang).map { addresses =>
          if (config.ordnanceSurveyUseUprn) Ok(views.html.disposal_of_vehicle.business_choose_your_address(form.fill(),
            setupTradeDetailsModel.traderBusinessName,
            setupTradeDetailsModel.traderPostcode,
            addresses))
          else Ok(views.html.disposal_of_vehicle.business_choose_your_address(form.fill(),
            setupTradeDetailsModel.traderBusinessName,
            setupTradeDetailsModel.traderPostcode,
            index(addresses)))
        }
      case None => Future.successful {
        Redirect(routes.SetUpTradeDetails.present())
      }
    }
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        request.cookies.getModel[SetupTradeDetailsFormModel] match {
          case Some(setupTradeDetails) =>
            implicit val session = clientSideSessionFactory.getSession(request.cookies)
            fetchAddresses(setupTradeDetails, showBusinessName = Some(true)).map { addresses =>
              if (config.ordnanceSurveyUseUprn)
                BadRequest(business_choose_your_address(formWithReplacedErrors(invalidForm),
                  setupTradeDetails.traderBusinessName,
                  setupTradeDetails.traderPostcode,
                  addresses))
              else
                BadRequest(business_choose_your_address(formWithReplacedErrors(invalidForm),
                  setupTradeDetails.traderBusinessName,
                  setupTradeDetails.traderPostcode,
                  index(addresses)))
            }
          case None => Future.successful {
            Logger.error("Failed to find dealer details, redirecting")
            Redirect(routes.SetUpTradeDetails.present())
          }
        },
      validForm =>
        request.cookies.getModel[SetupTradeDetailsFormModel] match {
          case Some(setupTradeDetailsModel) =>
            implicit val session = clientSideSessionFactory.getSession(request.cookies)
            if (config.ordnanceSurveyUseUprn)
              lookupUprn(validForm, setupTradeDetailsModel.traderBusinessName)
            else
              lookupAddressByPostcodeThenIndex(validForm, setupTradeDetailsModel)
          case None => Future {
            Logger.error("Failed to find dealer details, redirecting")
            Redirect(routes.SetUpTradeDetails.present())
          }
        }
    )
  }

  private def index(addresses: Seq[(String, String)]) = {
    addresses.map { case (uprn, address) => address}. // Extract the address.
      zipWithIndex. // Add an index for each address
      map { case (address, index) => (index.toString, address)} // Flip them around so index comes first.
  }

  private def formWithReplacedErrors(form: Form[BusinessChooseYourAddressFormModel])(implicit request: Request[_]) =
    form.replaceError(AddressSelectId, "error.required",
      FormError(key = AddressSelectId, message = "disposal_businessChooseYourAddress.address.required", args = Seq.empty)).
      distinctErrors

  private def fetchAddresses(model: SetupTradeDetailsFormModel, showBusinessName: Option[Boolean])(implicit session: ClientSideSession, lang: Lang) =
    addressLookupService.fetchAddressesForPostcode(model.traderPostcode, session.trackingId, showBusinessName = showBusinessName)

  private def lookupUprn(model: BusinessChooseYourAddressFormModel, traderName: String)
                        (implicit request: Request[_], session: ClientSideSession) = {
    val lookedUpAddress = addressLookupService.fetchAddressForUprn(model.uprnSelected.toString, session.trackingId)
    lookedUpAddress.map {
      case Some(addressModel) => nextPage(model, traderName, addressModel)
      case None => Redirect(routes.UprnNotFound.present())
    }
  }

  private def lookupAddressByPostcodeThenIndex(model: BusinessChooseYourAddressFormModel, setupBusinessDetailsForm: SetupTradeDetailsFormModel)
                                              (implicit request: Request[_], session: ClientSideSession): Future[Result] = {
    fetchAddresses(setupBusinessDetailsForm, showBusinessName = Some(false))(session, request2lang).map { addresses =>
      val indexSelected = model.uprnSelected.toInt
      if (indexSelected < addresses.length) {
        val lookedUpAddresses = index(addresses)
        val lookedUpAddress = lookedUpAddresses(indexSelected) match {
          case (index, address) => address
        }
        val addressModel = AddressModel(uprn = None, address = lookedUpAddress.split(",") map (line => line.trim))
        nextPage(model, setupBusinessDetailsForm.traderBusinessName, addressModel)
      }
      else {
        // Guard against IndexOutOfBoundsException
        Redirect(routes.UprnNotFound.present())
      }
    }
  }

  private def nextPage(model: BusinessChooseYourAddressFormModel, traderName: String, addressModel: AddressModel)
                      (implicit request: Request[_], session: ClientSideSession): Result = {
    val traderDetailsModel = TraderDetailsModel(traderName = traderName, traderAddress = addressModel)
    /* The redirect is done as the final step within the map so that:
     1) we are not blocking threads
     2) the browser does not change page before the future has completed and written to the cache. */
    Redirect(routes.VehicleLookup.present()).
      discardingCookie(EnterAddressManuallyCacheKey).
      withCookie(model).
      withCookie(traderDetailsModel)
  }
}
