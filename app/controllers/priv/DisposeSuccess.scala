package controllers.priv

import com.google.inject.Inject
import controllers.SurveyUrl
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config

class DisposeSuccess @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                               config: Config,
                               surveyUrl: SurveyUrl,
                               dateService: DateService) extends controllers.DisposeSuccess {
  protected override val isPrivateKeeper = true
  protected override val newDisposeFormTarget = controllers.routes.DisposeSuccess.newDisposal()
  protected override val exitDisposeFormTarget = controllers.routes.DisposeSuccess.exit()
  protected override val onMissingPresentCookies = Redirect(controllers.routes.VehicleLookup.present())
  protected override val onMissingNewDisposeCookies = Redirect(controllers.routes.SetUpTradeDetails.present())
  protected override val onNewDispose = Redirect(config.endUrl)
}
