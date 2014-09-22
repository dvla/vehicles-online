package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.disposal_of_vehicle.VehicleLookupFailure
import VehicleLookupFailure.{BeforeYouStartId, VehicleLookupId}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext

object VehicleLookupFailurePage extends Page with WebBrowserDSL {
  final val address = s"$applicationContext/vehicle-lookup-failure"
  final override val title: String = "Look-up was unsuccessful"

  override def url: String = WebDriverFactory.testUrl + address.substring(1)

  def beforeYouStart(implicit driver: WebDriver): Element = find(id(BeforeYouStartId)).get

  def vehicleLookup(implicit driver: WebDriver): Element = find(id(VehicleLookupId)).get
}