package controllers

import javax.inject.Inject
import models.BusinessChooseYourAddressFormModel.Form.AddressSelectId
import models.{BusinessChooseYourAddressViewModel, BusinessChooseYourAddressFormModel}
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import play.api.data.{Form, FormError}
import play.api.i18n.Lang
import play.api.mvc.{Action, Request, Result}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.{ClientSideSession, ClientSideSessionFactory}
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.model.{AddressModel, SetupTradeDetailsFormModel, TraderDetailsModel}
import common.views.helpers.FormExtensions.formBinding
import common.webserviceclients.addresslookup.AddressLookupService
import utils.helpers.Config
import views.html.disposal_of_vehicle.business_choose_your_address

class BusinessChooseYourAddress @Inject()(addressLookupService: AddressLookupService)
                                         (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                          config: Config) extends BusinessController {

  private[controllers] val form = Form(BusinessChooseYourAddressFormModel.Form.Mapping)

  protected val submitCall = routes.BusinessChooseYourAddress.submit()
  protected val manualAddressEntryCall = routes.EnterAddressManually.present()
  protected val backCall = routes.SetUpTradeDetails.present()
  protected val redirectBack = Redirect(routes.SetUpTradeDetails.present())
  protected val successResult = Redirect(routes.VehicleLookup.present())
  protected val onMissingTraderDetails = Redirect(routes.SetUpTradeDetails.present())

  def present = Action.async { implicit request =>
    request.cookies.getModel[SetupTradeDetailsFormModel] match {
      case Some(setupTradeDetailsModel) =>
        val session = clientSideSessionFactory.getSession(request.cookies)
        fetchAddresses(setupTradeDetailsModel, showBusinessName = Some(true))(session, request2lang).map { addresses =>
          logMessage(request.cookies.trackingId(), Info, "Presenting business choose your address view")
          Ok(views.html.disposal_of_vehicle.business_choose_your_address(
            BusinessChooseYourAddressViewModel(
              form.fill(),
              setupTradeDetailsModel.traderBusinessName,
              setupTradeDetailsModel.traderPostcode,
              setupTradeDetailsModel.traderEmail,
              addresses,
              submitCall, manualAddressEntryCall, backCall
            )
          ))
        }
      case None => Future.successful {redirectBack}
    }
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        request.cookies.getModel[SetupTradeDetailsFormModel] match {
          case Some(setupTradeDetails) =>
            implicit val session = clientSideSessionFactory.getSession(request.cookies)
            fetchAddresses(setupTradeDetails, showBusinessName = Some(true)).map { addresses =>
                BadRequest(business_choose_your_address(
                  BusinessChooseYourAddressViewModel(
                    formWithReplacedErrors(invalidForm),
                    setupTradeDetails.traderBusinessName,
                    setupTradeDetails.traderPostcode,
                    setupTradeDetails.traderEmail,
                    addresses,
                    submitCall, manualAddressEntryCall, backCall
                  )
                ))
            }
          case None => Future.successful {
            logMessage(request.cookies.trackingId(), Error, "Failed to find dealer details, redirecting")
            redirectBack
          }
        },
      validForm =>
        request.cookies.getModel[SetupTradeDetailsFormModel] match {
          case Some(setupTradeDetailsModel) =>
            implicit val session = clientSideSessionFactory.getSession(request.cookies)
              lookupAddressByPostcodeThenIndex(validForm, setupTradeDetailsModel)
          case None => Future {
            logMessage(request.cookies.trackingId(), Error, "Failed to find dealer details, redirecting")
            onMissingTraderDetails
          }
        }
    )
  }

  private def formWithReplacedErrors(form: Form[BusinessChooseYourAddressFormModel])(implicit request: Request[_]) =
    form.replaceError(
      AddressSelectId, "error.required",
      FormError(key = AddressSelectId,
        message = "disposal_businessChooseYourAddress.address.required",
        args = Seq.empty
      )
    ).distinctErrors

  private def fetchAddresses(model: SetupTradeDetailsFormModel, showBusinessName: Option[Boolean])
                            (implicit session: ClientSideSession, lang: Lang) =
    addressLookupService.fetchAddressesForPostcode(model.traderPostcode, session.trackingId)

  private def lookupAddressByPostcodeThenIndex(model: BusinessChooseYourAddressFormModel,
                                               setupBusinessDetailsForm: SetupTradeDetailsFormModel
                                                )
                                              (implicit request: Request[_],
                                               session: ClientSideSession
                                                ): Future[Result] = {
    fetchAddresses(setupBusinessDetailsForm, showBusinessName = Some(false))(session, request2lang).map { addresses =>
        val lookedUpAddress = model.uprnSelected
        val addressModel = AddressModel(uprn = None, address = lookedUpAddress.split(",") map (line => line.trim))
        nextPage(model, setupBusinessDetailsForm.traderBusinessName, addressModel, setupBusinessDetailsForm.traderEmail)
    }
  }

  private def nextPage(model: BusinessChooseYourAddressFormModel,
                       traderName: String,
                       addressModel: AddressModel,
                       traderEmail: Option[String])
                      (implicit request: Request[_], session: ClientSideSession): Result = {
    val traderDetailsModel = TraderDetailsModel(traderName = traderName, traderAddress = addressModel, traderEmail = traderEmail)
    /* The redirect is done as the final step within the map so that:
     1) we are not blocking threads
     2) the browser does not change page before the future has completed and written to the cache. */
    successResult
      .discardingCookie(EnterAddressManuallyCacheKey)
      .withCookie(model)
      .withCookie(traderDetailsModel)
  }
}
