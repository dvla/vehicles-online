package gov.uk.dvla.vehicles.dispose.stepdefs

import cucumber.api.java.en.{Then, Given}
import org.openqa.selenium.WebDriver
import pages.disposal_of_vehicle.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver
import uk.gov.dvla.vehicles.presentation.common.testhelpers.RandomVrmGenerator

class DocRefNumberSteps (webBrowserDriver: WebBrowserDriver) extends gov.uk.dvla.vehicles.dispose.helpers.AcceptanceTestHelper {
  implicit lazy val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  val commonSteps = new CommonSteps(webBrowserDriver)

  @Given("""^a correctly formatted document reference number "(.*)" has been entered$""")
  def a_correctly_formatted_document_reference_number_has_been_entered(docRefNo:String) = {
    commonSteps.goToVehicleLookupPage()
    // override doc ref no with test value
    VehicleLookupPage.vehicleRegistrationNumber.value = RandomVrmGenerator.uniqueVrm
    VehicleLookupPage.documentReferenceNumber.value = docRefNo
  }

  @Given("""^an incorrectly formatted document reference number "(.*)" has been entered$""")
  def an_incorrectly_formatted_document_reference_number_has_been_entered(docRefNo:String) = {
    a_correctly_formatted_document_reference_number_has_been_entered(docRefNo)
  }

  @Then("""^the document reference number "(.*)" is retained$""")
  def the_document_reference_number_is_retained(docRefNo:String) = {
    /*def unquoteString(quotedString: String): String = {
      val stringWithoutEnclosingQuotes = quotedString.drop(1).dropRight(1)
      stringWithoutEnclosingQuotes.replace("\\\"", "\"")
    }

    val webDriverCookies = webDriver.manage().getCookies.toSet
    val playCookies = webDriverCookies.map(c => Cookie(c.getName, unquoteString(c.getValue)))

    playCookies.getModel[VehicleLookupFormModel] match {
      case Some(VehicleLookupFormModel(referenceNumber, _)) =>
        referenceNumber should equal(docRefNo)
      case None =>
        assert(false, "No valid cookie exists for this model")
    }*/
  }


}
