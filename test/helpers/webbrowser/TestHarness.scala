package helpers.webbrowser

import helpers.common.ProgressBar
import ProgressBar.{fakeApplicationWithProgressBarFalse, fakeApplicationWithProgressBarTrue}
import play.api.test._
import org.openqa.selenium.WebDriver
import org.specs2.mutable.Around
import org.specs2.specification.Scope
import play.api.test.TestServer
import play.api.test.FakeApplication
import org.specs2.execute.{Result, AsResult}


// NOTE: Do *not* put any initialisation code in the class below, otherwise delayedInit() gets invoked twice
// which means around() gets invoked twice and everything is not happy.  Only lazy vals and defs are allowed,
// no vals or any other code blocks.

trait TestHarness {
  import WebBrowser._
  abstract class WebBrowser(val app: FakeApplication = fakeAppWithTestGlobal,
                            val port: Int = 9002,
                            implicit protected val webDriver: WebDriver = WebDriverFactory.webDriver
                             )
      extends Around with Scope with WebBrowserDSL {



    override def around[T: AsResult](t: => T): Result = {
      try {
        Helpers.running(TestServer(port, app))(AsResult.effectively(t))
      } finally {
        webDriver.quit()
      }
    }
  }

  abstract class ProgressBarTrue extends WebBrowser(app = fakeApplicationWithProgressBarTrue)
  abstract class ProgressBarFalse extends WebBrowser(app = fakeApplicationWithProgressBarFalse)

  abstract class HtmlUnitWithJs extends WebBrowser(webDriver = WebDriverFactory.webDriver(
    targetBrowser = "htmlUnit",
    javascriptEnabled = true)
  )

  object WebBrowser {
    private lazy val fakeAppWithTestGlobal: FakeApplication = FakeApplication(withGlobal = Some(TestGlobal))
  }
}