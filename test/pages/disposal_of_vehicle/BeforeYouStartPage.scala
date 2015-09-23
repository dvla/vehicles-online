package pages.disposal_of_vehicle

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebDriverFactory}
import views.disposal_of_vehicle.BeforeYouStart.NextId

object  BeforeYouStartPage extends Page {
  final val address = s"$applicationContext/before-you-start"
  final override val title: String = "Buying a vehicle into trade"
  final val titleCy: String = "Gwerthu cerbyd i'r fasnach foduron"

  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  def startNow(implicit driver: WebDriver) = find(id(NextId)).get
}