package pages.disposal_of_vehicle

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.find
import WebBrowser.id
import WebBrowser.Element
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebDriverFactory}
import views.disposal_of_vehicle.OnHandlerNotFound
import OnHandlerNotFound.ExitId

object OnHandlerNotFoundPage extends Page {
  final val address = s"$applicationContext/nosuchpage/"
  final override val title: String = "This page cannot be found"

  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}