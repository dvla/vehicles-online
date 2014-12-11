package pages.disposal_of_vehicle

import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.disposal_of_vehicle.DisposeSuccess
import DisposeSuccess.{ExitDisposalId, NewDisposalId}

object DisposeSuccessPage extends Page with WebBrowserDSL {
  final val address = s"$applicationContext/sell-to-the-trade-success"
  final override val title: String = "Summary"

  override def url: String = WebDriverFactory.testUrl + address.substring(1)

  def newDisposal(implicit driver: WebDriver): Element = find(id(NewDisposalId)).get

  def exitDisposal(implicit driver: WebDriver): Element = find(id(ExitDisposalId)).get

  def happyPath(implicit driver: WebDriver) = {
    go to DisposeSuccessPage
    click on DisposeSuccessPage.newDisposal
  }
}