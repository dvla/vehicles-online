package pages.disposal_of_vehicle

import org.openqa.selenium.WebDriver
import pages.BasePage
import helpers.Config
import helpers.WebBrowser

// TODO Export this class as top-level class. This 'trait' is required as a result of a bug in ScalaTest.
// See https://github.com/scalatest/scalatest/issues/174
trait VehicleLookupPage extends BasePage { this :WebBrowser =>

   object VehicleLookupPage extends VehicleLookupPage

   class VehicleLookupPage extends Page {

     override val url: String = Config.baseUrl + "disposal-of-vehicle/vehicle-lookup"

     def v5cReferenceNumber(implicit driver: WebDriver): TextField = textField(id("v5cReferenceNumber"))

     def v5cRegistrationNumber(implicit driver: WebDriver): TextField = textField(id("v5cRegistrationNumber"))

     def v5cKeeperName(implicit driver: WebDriver): TextField = textField(id("v5cKeeperName"))

     def v5cPostcode(implicit driver: WebDriver): TextField = textField(id("v5cPostcode"))

     def back(implicit driver: WebDriver): Element = find(id("backButton")).get

     def findVehicleDetails(implicit driver: WebDriver): Element = find(xpath("//button[@type='submit' and @name=\"action\"]")).get

   }
 }