package pages.disposal_of_vehicle

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.find
import WebBrowser.id
import WebBrowser.Element

import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebDriverFactory}
import views.disposal_of_vehicle.UprnNotFound
import UprnNotFound.{ManualaddressbuttonId, SetuptradedetailsbuttonId}

object UprnNotFoundPage extends Page {
  final val address = s"$applicationContext/uprn-not-found"
  final override val title: String = "Error confirming post code"

  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  def setupTradeDetails(implicit driver: WebDriver): Element = find(id(SetuptradedetailsbuttonId)).get

  def manualAddress(implicit driver: WebDriver): Element = find(id(ManualaddressbuttonId)).get
}