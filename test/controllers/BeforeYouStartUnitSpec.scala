package controllers

import Common.PrototypeHtml
import helpers.{UnitSpec, TestWithApplication}
import org.mockito.Mockito.when
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import play.api.libs.json.{JsString, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, LOCATION, OK, status}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class BeforeYouStartUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new TestWithApplication {
      val result = beforeYouStart.present(FakeRequest())
      status(result) should equal(OK)
    }

    "display prototype message when config set to true" in new TestWithApplication {
      val result = beforeYouStart.present(FakeRequest())
      contentAsString(result) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new TestWithApplication {
      val request = FakeRequest()
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      when(config.isPrototypeBannerVisible).thenReturn(false) // Stub this config value.
      when(config.googleAnalyticsTrackingId).thenReturn(None) // Stub this config value.
      when(config.assetsUrl).thenReturn(None) // Stub this config value.
      val beforeYouStartPrototypeNotVisible = new BeforeYouStart()

      val result = beforeYouStartPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit" should {
    "redirect to next page after the button is clicked" in new TestWithApplication {
      val result = beforeYouStart.submit(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }
  }

  "calling test csrf" should {
    // Note that this will work because it is not going through the csrf filter
    // If you try calling the same method via curl with csrf.protection on it will return a 403 forbidden
    "process the json and return the contained message text when not going through the csrf filter" in new TestWithApplication() {
      val json = Json.obj("text" -> JsString("hello"))
      val req = FakeRequest().withJsonBody(json)
      val result = beforeYouStart.testCsrf()(req)
      whenReady(result) { r =>
        status(result) should equal(OK)
        contentAsString(result) should equal("""You said "hello"""")
      }
    }
  }

  private lazy val beforeYouStart = injector.getInstance(classOf[BeforeYouStart])
}