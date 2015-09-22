package pages.disposal_of_vehicle

import models.DisposeFormModelBase.Form.BackId
import models.DisposeFormModelBase.Form.ConsentId
import models.DisposeFormModelBase.Form.DateOfDisposalId
import models.DisposeFormModelBase.Form.LossOfRegistrationConsentId
import models.DisposeFormModelBase.Form.MileageId
import models.DisposeFormModelBase.Form.SubmitId
import models.PrivateDisposeFormModel.Form.EmailOptionId
import models.PrivateDisposeFormModel.Form.EmailId
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.Checkbox
import WebBrowser.checkbox
import WebBrowser.TextField
import WebBrowser.textField
import WebBrowser.TelField
import WebBrowser.telField
import WebBrowser.RadioButton
import WebBrowser.radioButton
import WebBrowser.find
import WebBrowser.id
import WebBrowser.Element
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common
import common.helpers.webbrowser.Page
import common.helpers.webbrowser.WebDriverFactory
import common.mappings.Email.{EmailId => EmailEnterId, EmailVerifyId}
import common.mappings.OptionalToggle.{Invisible, Visible}

object DisposeForPrivateKeeperPage extends Page {
  final val address = s"$applicationContext/private/complete-and-confirm"
  final override val title: String = "Complete & confirm"

  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  def mileage(implicit driver: WebDriver): TelField = telField(id(MileageId))

  def dateOfDisposalDay(implicit driver: WebDriver): TelField = telField(id(s"${DateOfDisposalId}_day"))

  def dateOfDisposalMonth(implicit driver: WebDriver): TelField = telField(id(s"${DateOfDisposalId}_month"))

  def dateOfDisposalYear(implicit driver: WebDriver): TelField = telField(id(s"${DateOfDisposalId}_year"))

  def consent(implicit driver: WebDriver): Checkbox = checkbox(id(ConsentId))

  def emailField(implicit driver: WebDriver): TextField = textField(id(s"${EmailId}_$EmailEnterId"))

  def emailConfirmField(implicit driver: WebDriver): TextField = textField(id(s"${EmailId}_$EmailVerifyId"))

  def emailInvisible(implicit driver: WebDriver): RadioButton = radioButton(id(s"${EmailOptionId}_$Invisible"))

  def emailVisible(implicit driver: WebDriver): RadioButton = radioButton(id(s"${EmailOptionId}_$Visible"))

  def lossOfRegistrationConsent(implicit driver: WebDriver): Element = find(id(LossOfRegistrationConsentId)).get

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def dispose(implicit driver: WebDriver): Element = find(id(SubmitId)).get
}
