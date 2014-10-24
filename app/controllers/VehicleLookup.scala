package controllers

import _root_.play.api.mvc.Call
import com.google.inject.Inject
import play.api.Logger
import play.api.data.{Form => PlayForm, FormError}
import play.api.mvc.{Action, AnyContent, Controller, Request, Result}
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupBase
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupBase.LookupResult
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupBase.{VehicleFound, LookupResult, VehicleNotFound}
import uk.gov.dvla.vehicles.presentation.common.model.{TraderDetailsModel, VehicleDetailsModel, BruteForcePreventionModel}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehiclelookup.{VehicleLookupService, VehicleDetailsResponseDto, VehicleDetailsRequestDto, VehicleDetailsDto}
import utils.helpers.Config
import models.DisposeFormModel.{DisposeOccurredCacheKey, PreventGoingToDisposePageCacheKey, SurveyRequestTriggerDateCacheKey}
import models.{EnterAddressManuallyFormModel, VehicleLookupViewModel, AllCacheKeys, VehicleLookupFormModel}
import models.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class VehicleLookup @Inject()(val bruteForceService: BruteForcePreventionService,
                                    vehicleLookupService: VehicleLookupService,
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
    Redirect(routes.BeforeYouStart.present())
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
    val vehicleDetailsRequest = VehicleDetailsRequestDto(
      referenceNumber = form.referenceNumber,
      registrationNumber = form.registrationNumber,
      userName = request.cookies.getModel[TraderDetailsModel].fold("")(_.traderName)
    )

    vehicleLookupService.invoke(vehicleDetailsRequest, trackingId) map { response =>
      response.responseCode match {
        case Some(responseCode) =>
          VehicleNotFound(responseCode)

        case None =>
          response.vehicleDetailsDto match {
            case Some(dto) => 
              // US320: we have successfully called the lookup service so we cannot be coming back from a dispose success (as the doc id will have changed and the call sould fail).
              VehicleFound(Redirect(routes.Dispose.present()).
                withCookie(VehicleDetailsModel.fromDto(dto)).
                discardingCookie(PreventGoingToDisposePageCacheKey))
            case None => throw new RuntimeException("No vehicleDetailsDto found")
          }
      }
    }
  }
}
