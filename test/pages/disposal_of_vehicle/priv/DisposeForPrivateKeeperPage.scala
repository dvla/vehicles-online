package pages.disposal_of_vehicle.priv

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.click
import WebBrowser.go
import uk.gov.dvla.vehicles.presentation.common
import common.helpers.webbrowser.Page
import common.helpers.webbrowser.WebDriverFactory
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalDayValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalMonthValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalYearValid
import webserviceclients.fakes.FakeDisposeWebServiceImpl.MileageValid

object DisposeForPrivateKeeperPage extends pages.disposal_of_vehicle.DisposePageBase {
  final val address = pages.disposal_of_vehicle.buildAppUrl("private/complete-and-confirm")
  final override val title: String = "Complete and confirm"

  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  def happyPath(implicit driver: WebDriver) = {
    go to DisposeForPrivateKeeperPage
    click on lossOfRegistrationConsent
    dateOfDisposalDay.value = DateOfDisposalDayValid
    dateOfDisposalMonth.value = DateOfDisposalMonthValid
    dateOfDisposalYear.value = DateOfDisposalYearValid
    mileage.value = MileageValid
    click on emailInvisible

    click on dispose
  }
}
