package controllers

import com.google.inject.Inject
import play.api.data.{FormError, Form}
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, Call, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.controllers.FeedbackBase
import common.model.FeedbackForm
import common.model.FeedbackForm.Form.{feedback, nameMapping, emailMapping}
import common.views.helpers.FormExtensions.formBinding
import common.clientsidesession.CookieImplicits.RichCookies
import utils.helpers.Config
import webserviceclients.emailservice.EmailService

class FeedbackController @Inject()(val emailService: EmailService)
                                  (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                   config: Config) extends BusinessController with FeedbackBase {

  override val emailConfiguration = config.emailConfiguration
  protected val formTarget = controllers.routes.FeedbackController.submit()

  private[controllers] val form = Form(
    FeedbackForm.Form.Mapping
  )

  implicit val controls: Map[String, Call] = Map(
    "submit" -> formTarget
  )

  def present() = Action { implicit request =>
    Ok(views.html.disposal_of_vehicle.feedback(form))
  }

  def submit: Action[AnyContent] = Action { implicit request =>
    form.bindFromRequest.fold (
      invalidForm => BadRequest(views.html.disposal_of_vehicle.feedback(formWithReplacedErrors(invalidForm))),
        validForm => {
          val trackingId = request.cookies.trackingId
          sendFeedback(validForm, Messages("common_feedback.subject"), trackingId)
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
