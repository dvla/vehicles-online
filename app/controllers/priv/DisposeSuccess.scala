package controllers.priv

import controllers.SurveyUrl
import com.google.inject.Inject
import models.DisposeCacheKeyPrefix.CookiePrefix
import play.api.mvc.Action
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.model.TraderDetailsModel
import common.services.DateService
import utils.helpers.Config

class DisposeSuccess @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                               config: Config,
                               surveyUrl: SurveyUrl,
                               dateService: DateService)
  extends controllers.DisposeSuccess with PrivateKeeperController {

  override protected val newDisposeFormTarget = routes.DisposeSuccess.newDisposal()
  override protected val exitDisposeFormTarget = routes.DisposeSuccess.exit()
  override protected val onMissingPresentCookies = Redirect(routes.VehicleLookup.present())
  override protected val onMissingNewDisposeCookies = Redirect(routes.SetUpTradeDetails.present())

  override protected val DisposeFormTransactionIdCacheKey = models.DisposeFormModel.DisposeFormTransactionIdCacheKey
  override protected val DisposeFormRegistrationNumberCacheKey =
    models.DisposeFormModel.DisposeFormRegistrationNumberCacheKey
  override protected val DisposeFormTimestampIdCacheKey = models.DisposeFormModel.DisposeFormTimestampIdCacheKey
  override protected val PreventGoingToDisposePageCacheKey = models.DisposeFormModel.PreventGoingToDisposePageCacheKey
  override protected val SurveyRequestTriggerDateCacheKey = models.DisposeFormModel.SurveyRequestTriggerDateCacheKey
  override protected val DisposeOccurredCacheKey = models.DisposeFormModel.DisposeOccurredCacheKey

  override def newDisposal = Action { implicit request =>
    request.cookies.getModel[TraderDetailsModel].map { traderDetails =>
      Redirect(routes.NotifyAnotherSale.present())
    } getOrElse onMissingNewDisposeCookies
  }
}
