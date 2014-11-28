package views.disposal_of_vehicle

import helpers.{WireMockFixture, UiSpec}
import helpers.webbrowser.{WebDriverFactory, TestHarness}
import pages.ApplicationContext.applicationContext
import scala.io.Source.fromInputStream

class VersionIntegrationSpec extends UiSpec with TestHarness  with WireMockFixture {
  "Version endpoint" ignore {
    "be declared and should include the build-details.txt from classpath" in new WebBrowser {
      go.to(WebDriverFactory.testUrl + s"$applicationContext/version")
      val t = fromInputStream(getClass.getResourceAsStream("/build-details.txt")).getLines().toList
      page.source.lines.toSeq should contain allOf(t.head, t.tail.head, t.tail.tail.toSeq:_*)
    }
  }
}
