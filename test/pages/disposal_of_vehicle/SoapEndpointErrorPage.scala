package pages.disposal_of_vehicle

import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebDriverFactory}

object SoapEndpointErrorPage extends Page {
  final val address = s"$applicationContext/soap-endpoint-error"
  final override val title: String = "We are sorry"

  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)
}