package controllers

import javax.inject.Inject
import models.BusinessChooseYourAddressFormModel.Form.AddressSelectId
import models.{BusinessChooseYourAddressViewModel, BusinessChooseYourAddressFormModel}
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import play.api.data.{Form, FormError}
import play.api.i18n.Lang
import play.api.Logger
import play.api.mvc.{Action, Controller, Request, Result}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.{ClientSideSession, ClientSideSessionFactory}
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.LogFormats.logMessage
import common.model.{AddressModel, SetupTradeDetailsFormModel, TraderDetailsModel}
import common.views.helpers.FormExtensions.formBinding
import common.webserviceclients.addresslookup.AddressLookupService
import utils.helpers.Config
import views.html.disposal_of_vehicle.business_choose_your_address

class BusinessChooseYourAddress @Inject()(addressLookupService: AddressLookupService)
                                         (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                          config: Config) extends Controller {

  private[controllers] val form = Form(BusinessChooseYourAddressFormModel.Form.Mapping)

  protected val submitCall = routes.BusinessChooseYourAddress.submit
  protected val manualAddressEntryCall = routes.EnterAddressManually.present
  protected val backCall = routes.SetUpTradeDetails.present
  protected val redirectBack = Redirect(backCall)
  protected val uprnNotFoundCall = routes.UprnNotFound.present()
  protected val uprnNotFoundResult = Redirect(uprnNotFoundCall)
  protected val successCall = routes.VehicleLookup.present()
  protected val successResult = Redirect(successCall)
  protected val missingTraderDetailsCall = routes.SetUpTradeDetails.present()
  protected val onMissingTraderDetails = Redirect(missingTraderDetailsCall)

  def present = Action.async { implicit request =>
    request.cookies.getModel[SetupTradeDetailsFormModel] match {
      case Some(setupTradeDetailsModel) =>
        val session = clientSideSessionFactory.getSession(request.cookies)
        fetchAddresses(setupTradeDetailsModel, showBusinessName = Some(true))(session, request2lang).map { addresses =>
          if (config.ordnanceSurveyUseUprn)
            Ok(views.html.disposal_of_vehicle.business_choose_your_address(
              BusinessChooseYourAddressViewModel(
                form.fill(),
                setupTradeDetailsModel.traderBusinessName,
                setupTradeDetailsModel.traderPostcode,
                addresses,
                submitCall, manualAddressEntryCall, backCall
              )
            ))
          else Ok(views.html.disposal_of_vehicle.business_choose_your_address(
            BusinessChooseYourAddressViewModel(
              form.fill(),
              setupTradeDetailsModel.traderBusinessName,
              setupTradeDetailsModel.traderPostcode,
              index(addresses),
              submitCall, manualAddressEntryCall, backCall
            )
          ))
        }
      case None => Future.successful {
        Logger.error(logMessage(s"Failed to find dealer details, redirecting to ${backCall}",
          request.cookies.trackingId()))
        redirectBack
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
              if (config.ordnanceSurveyUseUprn){
              Logger.debug(logMessage(s"Failed to find address details, redirecting to ${manualAddressEntryCall}",
                request.cookies.trackingId()))
                BadRequest(business_choose_your_address(
                  BusinessChooseYourAddressViewModel(
                    formWithReplacedErrors(invalidForm),
                    setupTradeDetails.traderBusinessName,
                    setupTradeDetails.traderPostcode,
                    addresses,
                    submitCall, manualAddressEntryCall, backCall
                  )
                ))
              }
              else {
                Logger.debug(logMessage(s"Failed to find address details, redirecting to ${manualAddressEntryCall}",
                  request.cookies.trackingId()))
                BadRequest(business_choose_your_address(
                  BusinessChooseYourAddressViewModel(
                    formWithReplacedErrors(invalidForm),
                    setupTradeDetails.traderBusinessName,
                    setupTradeDetails.traderPostcode,
                    index(addresses),
                    submitCall, manualAddressEntryCall, backCall
                  )
                ))
              }
            }
          case None => Future.successful {
            Logger.error(logMessage("Failed to find dealer details, redirecting", request.cookies.trackingId()))
            redirectBack
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
            Logger.error(logMessage(s"Failed to find dealer details, redirecting to ${missingTraderDetailsCall}",
              request.cookies.trackingId()))
            onMissingTraderDetails
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
      case None => {
        Logger.debug(logMessage(s"Failed to find UPRN, redirecting to ${successCall}", request.cookies.trackingId()))
        uprnNotFoundResult
      }
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
        Logger.debug(logMessage(s"Failed to find UPRN, redirecting to ${successCall}", request.cookies.trackingId()))
        uprnNotFoundResult
      }
    }
  }

  private def nextPage(model: BusinessChooseYourAddressFormModel, traderName: String, addressModel: AddressModel)
                      (implicit request: Request[_], session: ClientSideSession): Result = {
    val traderDetailsModel = TraderDetailsModel(traderName = traderName, traderAddress = addressModel)
    /* The redirect is done as the final step within the map so that:
     1) we are not blocking threads
     2) the browser does not change page before the future has completed and written to the cache. */
    Logger.debug(logMessage(s"Address found, redirecting to ${successCall}", request.cookies.trackingId()))
    successResult.
      discardingCookie(EnterAddressManuallyCacheKey).
      withCookie(model).
      withCookie(traderDetailsModel)
  }
}
