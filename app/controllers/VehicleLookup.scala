package controllers

import com.google.inject.Inject
import models.DisposeCacheKeyPrefix.CookiePrefix
import models.DisposeFormModel.{DisposeOccurredCacheKey, PreventGoingToDisposePageCacheKey, SurveyRequestTriggerDateCacheKey}
import models.{EnterAddressManuallyFormModel, VehicleLookupViewModel, AllCacheKeys, VehicleLookupFormModel}
import models.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey
import play.api.mvc.{Action, Request, Result}
import play.api.data.{Form, FormError}
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.controllers.VehicleLookupBase1
import common.model.{TraderDetailsModel, VehicleAndKeeperDetailsModel}
import common.services.DateService
import common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsDto
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupService
import common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config

class VehicleLookup @Inject()(implicit bruteForceService: BruteForcePreventionService,
                              vehicleAndKeeperLookupService: VehicleAndKeeperLookupService,
                              surveyUrl: SurveyUrl,
                              dateService: DateService,
                              clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends VehicleLookupBase1[VehicleLookupFormModel] {

  override val form = Form(VehicleLookupFormModel.Form.Mapping)
  override val responseCodeCacheKey: String = VehicleLookupResponseCodeCacheKey

  override def vrmLocked: Result = Redirect(routes.VrmLocked.present())

  override def microServiceError: Result = Redirect(routes.MicroServiceError.present())

  override def vehicleLookupFailure: Result = Redirect(routes.VehicleLookupFailure.present())

  override def presentResult(implicit request: Request[_]) = {
    request.cookies.getModel[TraderDetailsModel] match {
      case Some(traderDetails) =>
        Ok(views.html.disposal_of_vehicle.vehicle_lookup(
        VehicleLookupViewModel(
          form.fill(),
          shouldDisplayExitButton(request, clientSideSessionFactory),
          surveyUrl(request),
          traderDetails.traderName,
          traderDetails.traderAddress.address
        )))
      case None => Redirect(routes.SetUpTradeDetails.present())
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
            traderDetails.traderAddress.address
          ))
        )
      case None => Redirect(routes.SetUpTradeDetails.present())
    }
  }

  override def vehicleFoundResult(vehicleAndKeeperDetailsDto: VehicleAndKeeperDetailsDto,
                                  formModel: VehicleLookupFormModel)
                                 (implicit request: Request[_]): Result = {
    val model = VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto)
    val suppressed = model.suppressedV5Flag.getOrElse(false)
    val disposed = model.keeperEndDate.isDefined

    (disposed, suppressed) match {
      case (_, true) => Redirect(routes.SuppressedV5C.present()).withCookie(model)
      case (true, false) => Redirect(routes.DuplicateDisposalError.present())
      case (false, _) => Redirect(routes.Dispose.present()).
        withCookie(VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto)).
        discardingCookie(PreventGoingToDisposePageCacheKey)
    }
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
