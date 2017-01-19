package pages.disposal_of_vehicle

import models.DisposeFormModelBase.Form.BackId
import models.DisposeFormModelBase.Form.ConsentId
import models.DisposeFormModelBase.Form.DateOfDisposalId
import models.DisposeFormModelBase.Form.LossOfRegistrationConsentId
import models.DisposeFormModelBase.Form.MileageId
import models.DisposeFormModelBase.Form.SubmitId
import models.DisposeFormModelBase.Form.TodaysDateOfDisposal
import models.DisposeFormModelBase.Form.EmailOptionId
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.Checkbox
import WebBrowser.checkbox
import WebBrowser.TelField
import WebBrowser.telField
import WebBrowser.RadioButton
import WebBrowser.radioButton
import WebBrowser.click
import WebBrowser.go
import WebBrowser.find
import WebBrowser.id
import WebBrowser.Element
import uk.gov.dvla.vehicles.presentation.common
import common.helpers.webbrowser.Page
import common.helpers.webbrowser.WebDriverFactory
import common.mappings.OptionalToggle.{Invisible, Visible}
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalDayValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalMonthValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfDisposalYearValid
import webserviceclients.fakes.FakeDisposeWebServiceImpl.MileageValid

trait DisposePageBase extends Page {
  def address: String

  def mileage(implicit driver: WebDriver): TelField = telField(id(MileageId))

  def dateOfDisposalDay(implicit driver: WebDriver): TelField = telField(id(s"${DateOfDisposalId}_day"))

  def dateOfDisposalMonth(implicit driver: WebDriver): TelField = telField(id(s"${DateOfDisposalId}_month"))

  def dateOfDisposalYear(implicit driver: WebDriver): TelField = telField(id(s"${DateOfDisposalId}_year"))

  def emailInvisible(implicit driver: WebDriver): RadioButton = radioButton(id(s"${EmailOptionId}_$Invisible"))

  def emailVisible(implicit driver: WebDriver): RadioButton = radioButton(id(s"${EmailOptionId}_$Visible"))

  def lossOfRegistrationConsent(implicit driver: WebDriver): Element = find(id(LossOfRegistrationConsentId)).get

  def useTodaysDate(implicit driver: WebDriver): Element = find(id(TodaysDateOfDisposal)).get

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def dispose(implicit driver: WebDriver): Element = find(id(SubmitId)).get
}

object DisposePage extends DisposePageBase {
  final val address = buildAppUrl("complete-and-confirm")
  final override val title: String = "Complete and confirm"

  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  def consent(implicit driver: WebDriver): Checkbox = checkbox(id(ConsentId))

  def happyPath(implicit driver: WebDriver) = {
    go to DisposePage
    mileage.value = MileageValid
    dateOfDisposalDay.value = DateOfDisposalDayValid
    dateOfDisposalMonth.value = DateOfDisposalMonthValid
    dateOfDisposalYear.value = DateOfDisposalYearValid
    click on consent
    click on lossOfRegistrationConsent
    click on DisposePage.emailInvisible

    click on dispose
  }

  def sadPath(implicit driver: WebDriver) = {
    go to DisposePage
    dateOfDisposalDay.value = ""
    dateOfDisposalMonth.value = ""
    dateOfDisposalYear.value = ""
    click on DisposePage.emailInvisible
    click on dispose
  }
}
