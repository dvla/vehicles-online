package pages.disposal_of_vehicle

import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import org.scalatest.selenium.WebBrowser.{enter, Checkbox, checkbox, TextField, textField, TelField, telField, RadioButton, radioButton, click, go, find, id, Element, pageSource, pageTitle}

object DisposeSuccessForPrivateKeeperPage extends Page {
  final val address = s"$applicationContext/private/sell-to-the-trade-success"
  final override val title: String = "Summary"
  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)
}
