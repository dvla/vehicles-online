package controllers

import com.google.inject.Inject
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, AnyContent, Controller, Request, Result}
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.model.{TraderDetailsModel, VehicleDetailsModel, BruteForcePreventionModel}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehiclelookup.{VehicleLookupService, VehicleDetailsResponseDto, VehicleDetailsRequestDto, VehicleDetailsDto}
import utils.helpers.Config
import models.DisposeFormModel.{DisposeOccurredCacheKey, PreventGoingToDisposePageCacheKey, SurveyRequestTriggerDateCacheKey}
import models.{VehicleLookupViewModel, AllCacheKeys, VehicleLookupFormModel}
import models.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class VehicleLookup @Inject()(bruteForceService: BruteForcePreventionService,
                                    vehicleLookupService: VehicleLookupService,
                                    surveyUrl: SurveyUrl,
                                    dateService: DateService)
                                   (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config) extends Controller {

  private[controllers] val form = Form(
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
        bruteForceAndLookup(validForm)
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
    request.cookies.getModel[TraderDetailsModel] match {
      case Some(dealerDetails) =>
        if (dealerDetails.traderAddress.uprn.isDefined) Redirect(routes.BusinessChooseYourAddress.present())
        else Redirect(routes.EnterAddressManually.present())
      case None => Redirect(routes.SetUpTradeDetails.present())
    }
  }

  private def bruteForceAndLookup(formModel: VehicleLookupFormModel)
                                 (implicit request: Request[_]): Future[Result] =

    bruteForceService.isVrmLookupPermitted(formModel.registrationNumber).flatMap { bruteForcePreventionViewModel =>
      // TODO US270 @Lawrence please code review the way we are using map, the lambda (I think we could use _ but it looks strange to read) and flatmap
      // US270: The security micro-service will return a Forbidden (403) message when the vrm is locked, we have hidden that logic as a boolean.
      if (bruteForcePreventionViewModel.permitted) lookupVehicleResult(formModel, bruteForcePreventionViewModel)
      else Future.successful {
        val registrationNumber = LogFormats.anonymize(formModel.registrationNumber)
        Logger.warn(s"BruteForceService locked out vrm: $registrationNumber")
        Redirect(routes.VrmLocked.present()).
          withCookie(bruteForcePreventionViewModel)
      }
    } recover {
      case exception: Throwable =>
        Logger.error(
          s"Exception thrown by BruteForceService so for safety we won't let anyone through. " +
          s"Exception ${exception.getStackTraceString}"
        )
        Redirect(routes.MicroServiceError.present())
    }

  private def lookupVehicleResult(model: VehicleLookupFormModel,
                                  bruteForcePreventionViewModel: BruteForcePreventionModel)
                                 (implicit request: Request[_]): Future[Result] = {
    def vehicleFoundResult(vehicleDetailsDto: VehicleDetailsDto) =
      Redirect(routes.Dispose.present()).
        withCookie(VehicleDetailsModel.fromDto(vehicleDetailsDto)).
        discardingCookie(PreventGoingToDisposePageCacheKey) // US320: we have successfully called the lookup service so we cannot be coming back from a dispose success (as the doc id will have changed and the call sould fail).

    def vehicleNotFoundResult(responseCode: String) = {
      val enterAddressManualHtml = views.html.disposal_of_vehicle.enter_address_manually
      val registrationNumber = LogFormats.anonymize(model.registrationNumber)
      Logger.debug(
        s"VehicleLookup encountered a problem with request $enterAddressManualHtml " +
        s"$registrationNumber, redirect to VehicleLookupFailure"
      )
      Redirect(routes.VehicleLookupFailure.present())
        .withCookie(key = VehicleLookupResponseCodeCacheKey, value = responseCode)
    }

    def microServiceErrorResult(message: String) = {
      Logger.error(message)
      Redirect(routes.MicroServiceError.present())
    }

    def microServiceThrowableResult(message: String, t: Throwable) = {
      Logger.error(message, t)
      Redirect(routes.MicroServiceError.present())
    }

    def createResultFromVehicleLookupResponse(vehicleDetailsResponse: VehicleDetailsResponseDto)
                                             (implicit request: Request[_]) =
      vehicleDetailsResponse.responseCode match {
        case Some(responseCode) => vehicleNotFoundResult(responseCode) // There is only a response code when there is a problem.
        case None =>
          // Happy path when there is no response code therefore no problem.
          vehicleDetailsResponse.vehicleDetailsDto match {
            case Some(dto) => vehicleFoundResult(dto)
            case None => microServiceErrorResult(message = "No vehicleDetailsDto found")
          }
      }

    def vehicleLookupSuccessResponse(responseStatusVehicleLookupMS: Int,
                                     vehicleDetailsResponse: Option[VehicleDetailsResponseDto])
                                    (implicit request: Request[_]) =
      responseStatusVehicleLookupMS match {
        case OK =>
          vehicleDetailsResponse match {
            case Some(response) => createResultFromVehicleLookupResponse(response)
            case _ => microServiceErrorResult("No vehicleDetailsResponse found") // TODO write test to achieve code coverage.
          }
        case _ => microServiceErrorResult(s"VehicleLookup web service call http status not OK, it was: " +
          s"$responseStatusVehicleLookupMS. Problem may come from either vehicle-lookup micro-service or the VSS")
      }

    val trackingId = request.cookies.trackingId()
    val vehicleDetailsRequest = VehicleDetailsRequestDto(
      referenceNumber = model.referenceNumber,
      registrationNumber = model.registrationNumber,
      userName = request.cookies.getModel[TraderDetailsModel].fold("")(_.traderName)
    )
    vehicleLookupService.invoke(vehicleDetailsRequest, trackingId).map {
      case (responseStatusVehicleLookupMS: Int, vehicleDetailsResponse: Option[VehicleDetailsResponseDto]) =>
        vehicleLookupSuccessResponse(
          responseStatusVehicleLookupMS = responseStatusVehicleLookupMS,
          vehicleDetailsResponse = vehicleDetailsResponse).
          withCookie(model).
          withCookie(bruteForcePreventionViewModel)
    }.recover {
      case e: Throwable => microServiceThrowableResult(message = s"VehicleLookup Web service call failed.", e)
    }
  }
}
