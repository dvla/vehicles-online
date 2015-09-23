package pages.disposal_of_vehicle

import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebDriverFactory}
import views.disposal_of_vehicle.MicroserviceError
import MicroserviceError.{ExitId, TryAgainId}
import org.scalatest.selenium.WebBrowser.{enter, Checkbox, checkbox, TextField, textField, TelField, telField, RadioButton, radioButton, click, go, find, id, Element, pageSource, pageTitle}

object DuplicateDisposalErrorPage extends Page {
  final val address = s"$applicationContext/duplicate-sell-to-the-trade-error"

  final override val title = "We are sorry"

  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  def tryAgain(implicit driver: WebDriver): Element = find(id(TryAgainId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}