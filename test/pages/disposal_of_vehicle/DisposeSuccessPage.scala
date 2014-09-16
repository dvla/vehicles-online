package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.disposal_of_vehicle.DisposeSuccess
import DisposeSuccess.{ExitDisposalId, NewDisposalId}
import org.openqa.selenium.WebDriver

object DisposeSuccessPage extends Page with WebBrowserDSL {
  final val address = "/sell-to-the-trade/sell-to-the-trade-success"
  final override val title: String = "Summary"

  override def url: String = WebDriverFactory.testUrl + address.substring(1)

  def newDisposal(implicit driver: WebDriver): Element = find(id(NewDisposalId)).get

  def exitDisposal(implicit driver: WebDriver): Element = find(id(ExitDisposalId)).get

  def happyPath(implicit driver: WebDriver) = {
    go to DisposeSuccessPage
    click on DisposeSuccessPage.newDisposal
  }
}