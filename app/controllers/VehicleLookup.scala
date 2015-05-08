package controllers

import com.google.inject.Inject
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.DisposeFormModel.{DisposeOccurredCacheKey, PreventGoingToDisposePageCacheKey, SurveyRequestTriggerDateCacheKey}
import models.{EnterAddressManuallyFormModel, VehicleLookupViewModel, AllCacheKeys, VehicleLookupFormModel}
import models.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, Request, Result}
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.controllers.VehicleLookupBase
import common.model.{BruteForcePreventionModel, TraderDetailsModel, VehicleAndKeeperDetailsModel}
import common.services.DateService
import common.views.helpers.FormExtensions.formBinding
import common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupDetailsDto
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupErrorMessage
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupService
import utils.helpers.Config

class VehicleLookup @Inject()(implicit bruteForceService: BruteForcePreventionService,
                              vehicleAndKeeperLookupService: VehicleAndKeeperLookupService,
                              surveyUrl: SurveyUrl,
                              dateService: DateService,
                              clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends VehicleLookupBase[VehicleLookupFormModel] {

  override val form = Form(VehicleLookupFormModel.Form.Mapping)
  override val responseCodeCacheKey: String = VehicleLookupResponseCodeCacheKey

  protected val submitTarget = controllers.routes.VehicleLookup.submit()
  protected val backTarget = controllers.routes.VehicleLookup.back()
  protected val exitTarget = controllers.routes.VehicleLookup.exit()
  protected val onVrmLocked = Redirect(routes.VrmLocked.present())
  protected val onMicroServiceError = Redirect(routes.MicroServiceError.present())
  protected val onVehicleLookupFailure = Redirect(routes.VehicleLookupFailure.present())
  protected val missingTradeDetails = Redirect(routes.SetUpTradeDetails.present())
  protected val enterAddressManually = Redirect(routes.EnterAddressManually.present())
  protected val businessChooseYourAddress = Redirect(routes.BusinessChooseYourAddress.present())
  protected val suppressedV5C = Redirect(routes.SuppressedV5C.present())
  protected val duplicateDisposalError = Redirect(routes.DuplicateDisposalError.present())
  protected val dispose = Redirect(routes.Dispose.present())
  protected val onExit = Redirect(routes.BeforeYouStart.present())

  override def vrmLocked(bruteForcePreventionModel: BruteForcePreventionModel, formModel: VehicleLookupFormModel)
                        (implicit request: Request[_]): Result = onVrmLocked

  override def microServiceError(t: Throwable, formModel: VehicleLookupFormModel)
                                (implicit request: Request[_]): Result = onMicroServiceError

  override def vehicleLookupFailure(responseCode: VehicleAndKeeperLookupErrorMessage, formModel: VehicleLookupFormModel)
                                   (implicit request: Request[_]): Result = onVehicleLookupFailure

  override def presentResult(implicit request: Request[_]) = {
    request.cookies.getModel[TraderDetailsModel] match {
      case Some(traderDetails) =>
        Ok(views.html.disposal_of_vehicle.vehicle_lookup(
        VehicleLookupViewModel(
          form.fill(),
          shouldDisplayExitButton(request, clientSideSessionFactory),
          surveyUrl(request),
          traderDetails.traderName,
          traderDetails.traderAddress.address,
          submitTarget,
          backTarget,
          exitTarget
        )))
      case None => missingTradeDetails
    }
  }

  override def invalidFormResult(invalidForm: Form[VehicleLookupFormModel])
                                (implicit request: Request[_]): Future[Result] = Future.successful {
    request.cookies.getModel[TraderDetailsModel] match {
      case Some(traderDetails) =>
        BadRequest(views.html.disposal_of_vehicle.vehicle_lookup(
          VehicleLookupViewModel(
            formWithReplacedErrors(invalidForm),
            shouldDisplayExitButton(request, clientSideSessionFactory),
            surveyUrl(request),
            traderDetails.traderName,
            traderDetails.traderAddress.address,
            submitTarget,
            backTarget,
            exitTarget
          ))
        )
      case None => missingTradeDetails
    }
  }

  override def vehicleFoundResult(vehicleAndKeeperDetailsDto: VehicleAndKeeperLookupDetailsDto,
                                  formModel: VehicleLookupFormModel)
                                 (implicit request: Request[_]): Result = {
    val model = VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto)
    val suppressed = model.suppressedV5Flag.getOrElse(false)
    val disposed = model.keeperEndDate.isDefined

    (disposed, suppressed) match {
      case (_, true) => suppressedV5C.withCookie(model)
      case (true, false) => duplicateDisposalError
      case (false, _) => dispose.
        withCookie(VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto)).
        discardingCookie(PreventGoingToDisposePageCacheKey)
    }
  }

  def exit = Action { implicit request =>
    onExit
      .discardingCookies(AllCacheKeys)
      .withCookie(SurveyRequestTriggerDateCacheKey, dateService.now.getMillis.toString)
  }

  def back = Action { implicit request =>
    request.cookies.getModel[EnterAddressManuallyFormModel] match {
      case Some(manualAddress) => enterAddressManually
      case None => businessChooseYourAddress
    }
  }

  private def formWithReplacedErrors(invalidForm: Form[VehicleLookupFormModel]) =
    invalidForm.replaceError(
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
        args = Seq.empty
      )
    ).distinctErrors

  private def shouldDisplayExitButton(implicit request: Request[_],
                                      clientSideSessionFactory: ClientSideSessionFactory): Boolean = {
    val session = clientSideSessionFactory.getSession(request.cookies)
    val encryptedCookieName = session.nameCookie(DisposeOccurredCacheKey).value
    val displayExitButton = request.cookies.exists(c => c.name == encryptedCookieName)
    displayExitButton
  }
}
