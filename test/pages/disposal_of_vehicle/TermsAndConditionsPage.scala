package pages.disposal_of_vehicle

import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}

object TermsAndConditionsPage extends Page {
  final val address = s"$applicationContext/tandc"
  final override val title: String = "Terms and Conditions"
  final val titleCy: String = "Amodau a Thelerau"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
}
