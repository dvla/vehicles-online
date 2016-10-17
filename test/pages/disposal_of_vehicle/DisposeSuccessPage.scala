package pages.disposal_of_vehicle

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.click
import WebBrowser.go
import WebBrowser.find
import WebBrowser.id
import WebBrowser.Element
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebDriverFactory}
import views.disposal_of_vehicle.DisposeSuccess
import DisposeSuccess.{ExitDisposalId, NewDisposalId}

trait DisposeSuccessPageBase extends Page {
  def address: String

  final val title: String = "Summary"
  lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  def newDisposal(implicit driver: WebDriver): Element = find(id(NewDisposalId)).get

  def exitDisposal(implicit driver: WebDriver): Element = find(id(ExitDisposalId)).get
}

object DisposeSuccessPage extends DisposeSuccessPageBase {
  final val address = s"$applicationContext/sell-to-the-trade-success"

  def happyPath(implicit driver: WebDriver) = {
    go to DisposeSuccessPage
    click on DisposeSuccessPage.newDisposal
  }
}
