package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.disposal_of_vehicle.VrmLocked
import VrmLocked.{ExitDisposalId, NewDisposalId}
import org.openqa.selenium.WebDriver

object VrmLockedPage extends Page with WebBrowserDSL {
  final val address = "/sell-to-the-trade/vrm-locked"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)

  final override val title = "Registration number is locked"

  def newDisposal(implicit driver: WebDriver): Element = find(id(NewDisposalId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitDisposalId)).get
}