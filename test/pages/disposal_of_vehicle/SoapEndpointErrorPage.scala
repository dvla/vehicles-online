package pages.disposal_of_vehicle

import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory}
import pages.ApplicationContext.applicationContext

object SoapEndpointErrorPage extends Page with WebBrowserDSL {
  final val address = s"$applicationContext/soap-endpoint-error"
  final override val title: String = "We are sorry"

  override def url: String = WebDriverFactory.testUrl + address.substring(1)
}