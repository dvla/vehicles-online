package gov.uk.dvla.vehicles.dispose.stepdefs

import cucumber.api.java.en.{Then, Given}
import org.openqa.selenium.WebDriver
import pages.disposal_of_vehicle.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

class VehicleRegNumberSteps (webBrowserDriver: WebBrowserDriver) extends gov.uk.dvla.vehicles.dispose.helpers.AcceptanceTestHelper {
  implicit lazy val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  val commonSteps = new CommonSteps(webBrowserDriver)

  @Given("""^a correctly formatted vehicle reference mark "(.*)" has been entered$""")
  def a_correctly_formatted_vehicle_reference_mark_has_been_entered(refMark:String) = {
    commonSteps.goToVehicleLookupPage()
    // override doc ref no with test value
    VehicleLookupPage.vehicleRegistrationNumber.value = refMark
    VehicleLookupPage.documentReferenceNumber.value = "11111111111"
  }

  @Given("""^an incorrectly formatted vehicle reference mark "(.*)" has been entered$""")
  def an_incorrectly_formatted_vehicle_reference_mark_has_been_entered(refMark:String) = {
    a_correctly_formatted_vehicle_reference_mark_has_been_entered(refMark:String)
  }

  @Then("""^the vehicle reference mark "(.*)" is retained$""")
  def the_vehicle_reference_mark_is_retained(refMark:String) = {
  }


}
