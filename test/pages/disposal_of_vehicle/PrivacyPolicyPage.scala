package pages.disposal_of_vehicle

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}

object PrivacyPolicyPage extends Page {

  def address = buildAppUrl("privacy-policy")

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Vehicle Management and Personalised Registration Online Privacy Policy"
}
