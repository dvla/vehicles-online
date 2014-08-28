package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.disposal_of_vehicle.MicroserviceError
import MicroserviceError.{ExitId, TryAgainId}
import org.openqa.selenium.WebDriver

object DuplicateDisposalErrorPage extends Page with WebBrowserDSL {
  final val address = "/sell-to-the-trade/duplicate-sell-to-the-trade-error"

  final override val title = "We are sorry"

  override def url: String = WebDriverFactory.testUrl + address.substring(1)

  def tryAgain(implicit driver: WebDriver): Element = find(id(TryAgainId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}