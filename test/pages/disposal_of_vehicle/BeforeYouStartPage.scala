package pages.disposal_of_vehicle

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.disposal_of_vehicle.BeforeYouStart
import BeforeYouStart.NextId
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext

object  BeforeYouStartPage extends Page with WebBrowserDSL {
  final val address = s"$applicationContext/before-you-start"
  final override val title: String = "Sell a vehicle into the motor trade"
  final val titleCy: String = "Cael gwared cerbyd i mewn i'r fasnach foduron"

  override def url: String = WebDriverFactory.testUrl + address.substring(1)

  def startNow(implicit driver: WebDriver): Element = find(id(NextId)).get
}