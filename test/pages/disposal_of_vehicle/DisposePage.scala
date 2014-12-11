package pages.disposal_of_vehicle

import models.DisposeFormModel.Form.{BackId, ConsentId, DateOfDisposalId, LossOfRegistrationConsentId, MileageId, SubmitId, TodaysDateOfDisposal}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Checkbox, Element, Page, SingleSel, TelField, WebBrowserDSL, WebDriverFactory}
import uk.gov.dvla.vehicles.presentation.common.mappings.DayMonthYear
import DayMonthYear.{DayId, MonthId, YearId}
import webserviceclients.fakes.FakeDateServiceImpl.{DateOfDisposalDayValid, DateOfDisposalMonthValid, DateOfDisposalYearValid}
import webserviceclients.fakes.FakeDisposeWebServiceImpl.MileageValid

object DisposePage extends Page with WebBrowserDSL {
  final val address = s"$applicationContext/complete-and-confirm"
  final override val title: String = "Complete & confirm"

  override def url: String = WebDriverFactory.testUrl + address.substring(1)

  def mileage(implicit driver: WebDriver): TelField = telField(id(MileageId))

  def dateOfDisposalDay(implicit driver: WebDriver): SingleSel = singleSel(id(s"${DateOfDisposalId}_$DayId"))

  def dateOfDisposalMonth(implicit driver: WebDriver): SingleSel = singleSel(id(s"${DateOfDisposalId}_$MonthId"))

  def dateOfDisposalYear(implicit driver: WebDriver): SingleSel = singleSel(id(s"${DateOfDisposalId}_$YearId"))

  def consent(implicit driver: WebDriver): Checkbox = checkbox(id(ConsentId))

  def lossOfRegistrationConsent(implicit driver: WebDriver): Element = find(id(LossOfRegistrationConsentId)).get

  def useTodaysDate(implicit driver: WebDriver): Element = find(id(TodaysDateOfDisposal)).get

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def dispose(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def happyPath(implicit driver: WebDriver) = {
    go to DisposePage
    mileage enter MileageValid
    dateOfDisposalDay select DateOfDisposalDayValid
    dateOfDisposalMonth select DateOfDisposalMonthValid
    dateOfDisposalYear select DateOfDisposalYearValid
    click on consent
    click on lossOfRegistrationConsent
    click on dispose
  }

  def sadPath(implicit driver: WebDriver) = {
    go to DisposePage
    dateOfDisposalDay select ""
    dateOfDisposalMonth select ""
    dateOfDisposalYear select ""
    click on dispose
  }
}
