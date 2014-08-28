package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.disposal_of_vehicle.DisposeFailure
import DisposeFailure.{SetupTradeDetailsId, VehicleLookupId}
import org.openqa.selenium.WebDriver

object DisposeFailurePage extends Page with WebBrowserDSL {
  final val address = "/sell-to-the-trade/sell-to-the-trade-failure"
  final override val title: String = "Sell a vehicle into the motor trade: failure"

  override def url: String = WebDriverFactory.testUrl + address.substring(1)

  def setuptradedetails(implicit driver: WebDriver): Element = find(id(SetupTradeDetailsId)).get

  def vehiclelookup(implicit driver: WebDriver): Element = find(id(VehicleLookupId)).get
}