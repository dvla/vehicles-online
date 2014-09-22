package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.disposal_of_vehicle.UprnNotFound
import UprnNotFound.{ManualaddressbuttonId, SetuptradedetailsbuttonId}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext

object UprnNotFoundPage extends Page with WebBrowserDSL {
  final val address = s"$applicationContext/uprn-not-found"
  final override val title: String = "Error confirming post code"

  override def url: String = WebDriverFactory.testUrl + address.substring(1)

  def setupTradeDetails(implicit driver: WebDriver): Element = find(id(SetuptradedetailsbuttonId)).get

  def manualAddress(implicit driver: WebDriver): Element = find(id(ManualaddressbuttonId)).get
}