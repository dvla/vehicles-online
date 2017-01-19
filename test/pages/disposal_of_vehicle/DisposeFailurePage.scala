package pages.disposal_of_vehicle

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.find
import WebBrowser.id
import WebBrowser.Element
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebDriverFactory}
import views.disposal_of_vehicle.DisposeFailure
import DisposeFailure.{SetupTradeDetailsId, VehicleLookupId}

object DisposeFailurePage extends Page {
  final val address = buildAppUrl("sell-to-the-trade-failure")
  final override val title: String = "Buying a vehicle into trade: failure"

  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  def setuptradedetails(implicit driver: WebDriver): Element = find(id(SetupTradeDetailsId)).get

  def vehiclelookup(implicit driver: WebDriver): Element = find(id(VehicleLookupId)).get
}