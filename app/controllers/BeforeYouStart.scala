package controllers

import com.google.inject.Inject
import models.TestCsrfMessage
import play.api.mvc.Action
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import utils.helpers.Config

class BeforeYouStart @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                 config: Config) extends BusinessController {

  def present = Action { implicit request =>
    Ok(views.html.disposal_of_vehicle.before_you_start())
      .withNewSession
      .discardingCookies(AllCacheKeys)
  }

  def submit = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Debug,
      s"Redirecting from BeforeYouStart to ${routes.SetUpTradeDetails.present()}")
    Redirect(routes.SetUpTradeDetails.present())
  }

  /**
    * This method is here to allow us to test the Cross Site Request Forgery protection is
    * working as expected. We have a custom CSRF implementation that prevents JSON post
    * requests from being accepted. This method accepts a JSON post request so we can use
    * it for testing. With CSRF protection enabled it should return an http 403 when trying
    * to call this method.
    *
    * Call it like this:
    * curl -v localhost:9000/test-csrf -H "Content-type: application/json" -d '{"text":"hello"}'
    *
    * @return an http 200 result with the text that is in the json message
    */
  def testCsrf = Action { request =>
    val json = request.body.asJson.get
    val message = json.as[TestCsrfMessage]
    val msg = s"json = $json, deserialized = $message"
    logMessage(request.cookies.trackingId(), Debug, msg)
    Ok(s"""You said \"${message.text}\"""")
  }
}