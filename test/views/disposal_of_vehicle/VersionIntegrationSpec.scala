package views.disposal_of_vehicle

import composition.TestHarness
import helpers.{WireMockFixture, UiSpec}
import org.scalatest.selenium.WebBrowser
import WebBrowser.enter
import WebBrowser.Checkbox
import WebBrowser.checkbox
import WebBrowser.TextField
import WebBrowser.textField
import WebBrowser.TelField
import WebBrowser.telField
import WebBrowser.RadioButton
import WebBrowser.radioButton
import WebBrowser.click
import WebBrowser.go
import WebBrowser.find
import WebBrowser.id
import WebBrowser.Element
import WebBrowser.pageSource
import WebBrowser.pageTitle
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
