package controllers

import com.google.inject.Inject
import play.api.data.{FormError, Form}
import play.api.i18n.Messages
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.controllers.FeedbackBase
import uk.gov.dvla.vehicles.presentation.common.model.FeedbackForm
import uk.gov.dvla.vehicles.presentation.common.model.FeedbackForm.Form._
import common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config

class FeedbackController @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                        config: Config) extends Controller with FeedbackBase {

  override val emailConfiguration = config.emailConfiguration

  private[controllers] val form = Form(
    FeedbackForm.Form.Mapping
  )

  implicit val controls: Map[String, Call] = Map(
  "submit" -> controllers.routes.FeedbackController.submit()
  )

  def present() = Action { implicit request =>
    Ok(views.html.disposal_of_vehicle.feedback(form))
  }

  def submit: Action[AnyContent] = Action { implicit request =>
    form.bindFromRequest.fold (
      invalidForm => BadRequest(views.html.disposal_of_vehicle.feedback(formWithReplacedErrors(invalidForm))),
        validForm => {
          sendFeedback(validForm, Messages("common_feedback.subject"))
          Ok(views.html.disposal_of_vehicle.feedbackSuccess())
        }
    )
  }

  private def formWithReplacedErrors(form: Form[FeedbackForm]) = {
    form.replaceError(
      feedback, FormError(key = feedback,message = "error.feedback", args = Seq.empty)
    ).replaceError(
        nameMapping, FormError(key = nameMapping, message = "error.feedbackName", args = Seq.empty)
      ).replaceError(
        emailMapping, FormError(key = emailMapping, message = "error.email", args = Seq.empty)
      ).distinctErrors
  }

}