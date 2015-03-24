package pages.disposal_of_vehicle

import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory}

object DisposeSuccessForPrivateKeeperPage extends Page with WebBrowserDSL {
  final val address = s"$applicationContext/sell-to-the-trade-private-keeper-success"
  final override val title: String = "Summary"
  override def url: String = WebDriverFactory.testUrl + address.substring(1)
}
