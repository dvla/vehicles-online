package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.disposal_of_vehicle.Error
import Error.SubmitId
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext

object ErrorPage extends Page with WebBrowserDSL {
  final val exceptionDigest = "fake-exception-digest"
  final val address = s"$applicationContext/error/" + exceptionDigest
  final override val title: String = "An unrecoverable error has occurred"

  override def url: String = WebDriverFactory.testUrl + address.substring(1)

  def startAgain(implicit driver: WebDriver): Element = find(id(SubmitId)).get
}