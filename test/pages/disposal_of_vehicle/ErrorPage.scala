package pages.disposal_of_vehicle

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.find
import WebBrowser.id
import WebBrowser.Element
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebDriverFactory}
import views.disposal_of_vehicle.Error
import Error.SubmitId

object ErrorPage extends Page {
  final val exceptionDigest = "fake-exception-digest"
  final val address = buildAppUrl("error/" + exceptionDigest)
  final override val title: String = "An unrecoverable error has occurred"

  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  def startAgain(implicit driver: WebDriver): Element = find(id(SubmitId)).get
}
