package controllers

import _root_.play.api.mvc.Call
import com.google.inject.Inject
import models.DisposeFormModel.{DisposeOccurredCacheKey, PreventGoingToDisposePageCacheKey, SurveyRequestTriggerDateCacheKey}
import models.{EnterAddressManuallyFormModel, VehicleLookupViewModel, AllCacheKeys, VehicleLookupFormModel}
import models.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey
import org.joda.time.DateTime
import play.api.data.{Form => PlayForm, FormError}
import play.api.mvc.{Action, AnyContent, Request}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.controllers.VehicleLookupBase
import common.controllers.VehicleLookupBase.{VehicleFound, LookupResult, VehicleNotFound}
import common.model.{TraderDetailsModel, VehicleAndKeeperDetailsModel}
import common.services.DateService
import common.views.helpers.FormExtensions.formBinding
import common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import common.webserviceclients.common.{DmsWebHeaderDto, DmsWebEndUserDto}
import common.webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperDetailsRequest, VehicleAndKeeperLookupService}
import utils.helpers.Config

final class VehicleLookup @Inject()(val bruteForceService: BruteForcePreventionService,
                                    vehicleAndKeeperLookupService: VehicleAndKeeperLookupService,
                                    surveyUrl: SurveyUrl,
                                    dateService: DateService)
                                   (implicit val clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config) extends VehicleLookupBase {

  override val vrmLocked: Call = routes.VrmLocked.present()
  override val microServiceError: Call = routes.MicroServiceError.present()
  override val vehicleLookupFailure: Call = routes.VehicleLookupFailure.present()
  override val responseCodeCacheKey: String = VehicleLookupResponseCodeCacheKey

  override type Form = VehicleLookupFormModel

  private[controllers] val form = PlayForm(
    VehicleLookupFormModel.Form.Mapping
  )

  def present = Action { implicit request =>
    request.cookies.getModel[TraderDetailsModel] match {
      case Some(traderDetails) =>
        Ok(views.html.disposal_of_vehicle.vehicle_lookup(
        VehicleLookupViewModel(
          form.fill(),
          shouldDisplayExitButton,
          surveyUrl(request),
          traderDetails.traderName,
          traderDetails.traderAddress.address
        )))
      case None => Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => Future.successful {
        request.cookies.getModel[TraderDetailsModel] match {
          case Some(traderDetails) =>
            val formWithReplacedErrors = invalidForm.replaceError(
              VehicleLookupFormModel.Form.VehicleRegistrationNumberId,
              FormError(
                key = VehicleLookupFormModel.Form.VehicleRegistrationNumberId,
                message = "error.restricted.validVrnOnly",
                args = Seq.empty
              )
            ).replaceError(
                VehicleLookupFormModel.Form.DocumentReferenceNumberId,
                FormError(
                  key = VehicleLookupFormModel.Form.DocumentReferenceNumberId,
                  message = "error.validDocumentReferenceNumber",
                  args = Seq.empty)
              ).distinctErrors

            BadRequest(views.html.disposal_of_vehicle.vehicle_lookup(
              VehicleLookupViewModel(
                formWithReplacedErrors,
                shouldDisplayExitButton,
                surveyUrl(request),
                traderDetails.traderName,
                traderDetails.traderAddress.address
            )))
          case None => Redirect(routes.SetUpTradeDetails.present())
        }
      },
      validForm => {
        bruteForceAndLookup(
          validForm.registrationNumber,
          validForm.referenceNumber,
          validForm)
      }
    )
  }

  private def shouldDisplayExitButton(implicit request: Request[AnyContent],
                                      clientSideSessionFactory: ClientSideSessionFactory): Boolean = {
    val session = clientSideSessionFactory.getSession(request.cookies)
    val encryptedCookieName = session.nameCookie(DisposeOccurredCacheKey).value
    val displayExitButton = request.cookies.exists(c => c.name == encryptedCookieName)
    displayExitButton
  }

  def exit = Action { implicit request =>
    Redirect(config.endUrl)
      .discardingCookies(AllCacheKeys)
      .withCookie(SurveyRequestTriggerDateCacheKey, dateService.now.getMillis.toString)
  }

  def back = Action { implicit request =>
    request.cookies.getModel[EnterAddressManuallyFormModel] match {
      case Some(manualAddress) =>
        Redirect(routes.EnterAddressManually.present())
      case None => Redirect(routes.BusinessChooseYourAddress.present())
    }
  }

  override protected def callLookupService(trackingId: String, form: Form)(implicit request: Request[_]): Future[LookupResult] = {
    val vehicleAndKeeperDetailsRequest = VehicleAndKeeperDetailsRequest(
      dmsHeader = buildHeader(trackingId),
      referenceNumber = form.referenceNumber,
      registrationNumber = form.registrationNumber,
      transactionTimestamp = new DateTime
    )

    vehicleAndKeeperLookupService.invoke(vehicleAndKeeperDetailsRequest, trackingId) map { response =>
      response.responseCode match {
        case Some(responseCode) =>
          VehicleNotFound(responseCode)
        case None =>
          response.vehicleAndKeeperDetailsDto match {
            case Some(dto) =>
//              VehicleFound(vehicleFoundResult(dto, form.vehicleSoldTo))
              // US320: we have successfully called the lookup service so we cannot be coming back from a dispose
              // success (as the doc id will have changed and the call should fail).
              VehicleFound(Redirect(routes.Dispose.present()).
                withCookie(VehicleAndKeeperDetailsModel.from(dto)).
                discardingCookie(PreventGoingToDisposePageCacheKey))

            case None => throw new RuntimeException("No vehicleAndKeeperDetailsDto found")
          }
      }
    }
  }

  private def buildHeader(trackingId: String): DmsWebHeaderDto = {
    val alwaysLog = true
    val englishLanguage = "EN"
    DmsWebHeaderDto(conversationId = trackingId,
      originDateTime = new DateTime,
      applicationCode = config.applicationCode,
      channelCode = config.channelCode,
      contactId = config.contactId,
      eventFlag = alwaysLog,
      serviceTypeCode = config.serviceTypeCode,
      languageCode = englishLanguage,
      endUser = None)
  }
}
