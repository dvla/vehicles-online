package pages.disposal_of_vehicle

import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory}

object SoapEndpointErrorPage extends Page with WebBrowserDSL {
  final val address = "/sell-to-the-trade/soap-endpoint-error"
  final override val title: String = "We are sorry"

  override def url: String = WebDriverFactory.testUrl + address.substring(1)
}