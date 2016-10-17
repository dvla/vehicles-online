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

class NotifyAnotherSale @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                               config: Config,
                               surveyUrl: SurveyUrl,
                               dateService: DateService)
  extends PrivateKeeperController {

  private[controllers] val form = play.api.data.Form(models.priv.NotifyAnotherSaleFormModel.Form.Mapping)

  private val PreventGoingToDisposePageCacheKey = models.DisposeFormModel.PreventGoingToDisposePageCacheKey
  private val DisposeOccurredCacheKey = models.DisposeFormModel.DisposeOccurredCacheKey

  def present = Action { implicit request =>
    request.cookies.getModel[TraderDetailsModel].map { traderDetails =>
      logMessage(request.cookies.trackingId(), Info, "Presenting private notify another sale view")
      Ok(views.html.disposal_of_vehicle.priv.notify_another_sale(
        form,
        traderDetails,
        routes.NotifyAnotherSale.submit()
      ))
    } getOrElse Redirect(routes.SetUpTradeDetails.present())
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => {
        request.cookies.getModel[TraderDetailsModel].map { traderDetails =>
            BadRequest(views.html.disposal_of_vehicle.priv.notify_another_sale(
            invalidForm,
            traderDetails,
            routes.NotifyAnotherSale.submit()
            ))
        } getOrElse Redirect(routes.SetUpTradeDetails.present())
      }
    , form => {
        val result =
          if (form.selection == "new_trader") Redirect(routes.SetUpTradeDetails.reset())
          else Redirect(routes.VehicleLookup.present())

        logMessage(request.cookies.trackingId(), Info, s"Redirect to $result")

        result
          .discardingCookies(DisposeCacheKeys)
          .withCookie(PreventGoingToDisposePageCacheKey, "")
          .withCookie(DisposeOccurredCacheKey, "")
      }
    )
  }
}
