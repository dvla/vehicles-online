package pages.disposal_of_vehicle

import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}

object CookiePolicyPage extends Page {

  def address = s"$applicationContext/cookie-policy"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Cookies"
  final val titleCy: String = "Cwcis"
}
