package views.disposal_of_vehicle

import com.google.inject.name.Names
import com.google.inject.{Guice, Injector}
import com.tzavellas.sse.guice.ScalaModule
import composition.{GlobalLike, TestComposition, TestHarness}
import helpers.UiSpec
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.impl.client.HttpClients
import org.scalatest.mock.MockitoSugar
import pages.disposal_of_vehicle.{BeforeYouStartPage, BusinessChooseYourAddressPage}
import pages.ApplicationContext.applicationContext
import play.api.LoggerLike
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter
import AccessLoggingFilter.AccessLoggerName
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{WebBrowserDSL, WebDriverFactory}
import uk.gov.dvla.vehicles.presentation.common.testhelpers.LightFakeApplication

class AccessLoggingIntegrationSpec extends UiSpec with TestHarness with MockitoSugar with WebBrowserDSL {

  "Access Logging" should {
    "Log access that complete with success" in new WebBrowserForSelenium(testApp1) {
      go to BeforeYouStartPage

      val infoLogs = mockLoggerTest1.captureLogInfos(1)
      infoLogs.get(0) should include( s"""] "GET $applicationContext/before-you-start HTTP/1.1" 200""")
    }

    "Log access that are completed because of Exception" in new WebBrowserForSelenium(testApp2) {
      val httpClient = HttpClients.createDefault()
      val post = new HttpPost(BusinessChooseYourAddressPage.url)
      val httpResponse = httpClient.execute(post)
      httpResponse.close()

      val infoLogs = mockLoggerTest2.captureLogInfos(1)
      infoLogs.get(0) should include( s"""] "POST $applicationContext/business-choose-your-address HTTP/1.1" 403""")
      //infoLogs.get(1) should include( s"""] "GET $applicationContext/error/""")
    }

    "Log access to unknown urls" in new WebBrowserForSelenium(testApp3) {
      val httpClient = HttpClients.createDefault()
      val post = new HttpPost(WebDriverFactory.testUrl + "/some/unknown/url")
      val httpResponse = httpClient.execute(post)
      httpResponse.close()

      val infoLogs = mockLoggerTest3.captureLogInfos(1)

      infoLogs.get(0) should include( """] "POST /some/unknown/url HTTP/1.1" 403""")
      //infoLogs.get(1) should include( s"""] "GET $applicationContext/error/""")
    }

    "not log any access for the healthcheck url" in new WebBrowserForSelenium(testApp4) {
      val httpClient = HttpClients.createDefault()
      val post = new HttpGet(WebDriverFactory.testUrl + s"$applicationContext/healthcheck")
      val httpResponse = httpClient.execute(post)
      httpResponse.close()

      val infoLogs = mockLoggerTest4.captureLogInfos(0)
    }

    "not log any access for the healthcheck url with parameters" in new WebBrowserForSelenium(testApp5) {
      val httpClient = HttpClients.createDefault()
      val post = new HttpGet(WebDriverFactory.testUrl + s"$applicationContext/healthcheck?param1=a&b=c")
      val httpResponse = httpClient.execute(post)
      httpResponse.close()

      val infoLogs = mockLoggerTest5.captureLogInfos(0)
    }

    "log any access for the healthcheck url that has extra in the path or parameters" in
      new WebBrowserForSelenium(testApp6) {
      val httpClient = HttpClients.createDefault()
      val post = new HttpGet(WebDriverFactory.testUrl + "/healthcheck/some/extra")
      val httpResponse = httpClient.execute(post)
      httpResponse.close()

      val infoLogs = mockLoggerTest6.captureLogInfos(1)
    }
  }

  private class TestGlobalWithMockLogger(mockLogger: MockLogger) extends GlobalLike with TestComposition {

    override lazy val injector: Injector = Guice.createInjector(testModule(new ScalaModule {
      override def configure(): Unit = {
        bind[LoggerLike].annotatedWith(Names.named(AccessLoggerName)).toInstance(mockLogger)
      }
    }))
  }

  private val mockLoggerTest1 = new MockLogger

  private def testApp1 = LightFakeApplication(new TestGlobalWithMockLogger(mockLoggerTest1))

  private val mockLoggerTest2 = new MockLogger

  private def testApp2 = LightFakeApplication(new TestGlobalWithMockLogger(mockLoggerTest2))

  private val mockLoggerTest3 = new MockLogger

  private def testApp3 = LightFakeApplication(new TestGlobalWithMockLogger(mockLoggerTest3))

  private val mockLoggerTest4 = new MockLogger

  private def testApp4 = LightFakeApplication(new TestGlobalWithMockLogger(mockLoggerTest4))

  private val mockLoggerTest5 = new MockLogger

  private def testApp5 = LightFakeApplication(new TestGlobalWithMockLogger(mockLoggerTest5))

  private val mockLoggerTest6 = new MockLogger

  private def testApp6 = LightFakeApplication(new TestGlobalWithMockLogger(mockLoggerTest6))
}
