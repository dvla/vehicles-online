package views.disposal_of_vehicle

import composition.TestHarness
import helpers.{WireMockFixture, UiSpec}
import org.scalatest.selenium.WebBrowser
import WebBrowser.go
import WebBrowser.pageSource
import pages.ApplicationContext.applicationContext
import scala.io.Source.fromInputStream
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.WebDriverFactory

class VersionIntegrationSpec extends UiSpec with TestHarness with WireMockFixture {
  "Version endpoint" should {
    "be declared and should include the build-details.txt from classpath" in new WebBrowserForSelenium {
      go.to(WebDriverFactory.testUrl + s"$applicationContext/version")
      val t = fromInputStream(getClass.getResourceAsStream("/build-details.txt")).getLines().toSet.toList
      pageSource.lines.toSet should contain allOf(t.head, t.tail.head, t.tail.tail.toSeq:_*)
    }
  }
}
