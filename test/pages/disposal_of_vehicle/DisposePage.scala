package pages.disposal_of_vehicle

//import models.DisposeFormModel.Form._
import models.DisposeFormModel.Form.MileageId
import models.DisposeFormModel.Form.DateOfDisposalId
import models.DisposeFormModel.Form.ConsentId
import models.DisposeFormModel.Form.EmailOptionId
import models.DisposeFormModel.Form.LossOfRegistrationConsentId
import models.DisposeFormModel.Form.TodaysDateOfDisposal
import models.DisposeFormModel.Form.BackId
import models.DisposeFormModel.Form.SubmitId
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.Checkbox
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.Element
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.Page
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.RadioButton
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.TelField
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDSL
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import common.mappings.OptionalToggle.Invisible
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalDayValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalMonthValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalYearValid
import webserviceclients.fakes.FakeDisposeWebServiceImpl.MileageValid

object DisposePage extends Page with WebBrowserDSL {
  final val address = s"$applicationContext/complete-and-confirm"
  final override val title: String = "Complete & confirm"

  override def url: String = WebDriverFactory.testUrl + address.substring(1)

  def mileage(implicit driver: WebDriver): TelField = telField(id(MileageId))

  def dateOfDisposalDay(implicit driver: WebDriver): TelField = telField(id(s"${DateOfDisposalId}_day"))

  def dateOfDisposalMonth(implicit driver: WebDriver): TelField = telField(id(s"${DateOfDisposalId}_month"))

  def dateOfDisposalYear(implicit driver: WebDriver): TelField = telField(id(s"${DateOfDisposalId}_year"))

  def consent(implicit driver: WebDriver): Checkbox = checkbox(id(ConsentId))

  def emailInvisible(implicit driver: WebDriver): RadioButton = radioButton(id(s"${EmailOptionId}_$Invisible"))

  def lossOfRegistrationConsent(implicit driver: WebDriver): Element = find(id(LossOfRegistrationConsentId)).get

  def useTodaysDate(implicit driver: WebDriver): Element = find(id(TodaysDateOfDisposal)).get

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def dispose(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def happyPath(implicit driver: WebDriver) = {
    go to DisposePage
    mileage enter MileageValid
    dateOfDisposalDay enter DateOfDisposalDayValid
    dateOfDisposalMonth enter DateOfDisposalMonthValid
    dateOfDisposalYear enter DateOfDisposalYearValid
    //click on emailInvisible
    click on consent
    click on lossOfRegistrationConsent
    click on dispose
  }

  def sadPath(implicit driver: WebDriver) = {
    go to DisposePage
    dateOfDisposalDay enter ""
    dateOfDisposalMonth enter ""
    dateOfDisposalYear enter ""
    click on dispose
  }
}
