package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.disposal_of_vehicle.OnHandlerNotFound
import OnHandlerNotFound.ExitId
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext

object OnHandlerNotFoundPage extends Page with WebBrowserDSL {
  final val address = s"$applicationContext/nosuchpage/"
  final override val title: String = "This page cannot be found"

  override def url: String = WebDriverFactory.testUrl + address.substring(1)

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}