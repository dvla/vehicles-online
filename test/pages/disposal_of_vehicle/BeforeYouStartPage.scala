package pages.disposal_of_vehicle

import com.typesafe.config.ConfigFactory
import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext
import views.disposal_of_vehicle.BeforeYouStart.NextId

object  BeforeYouStartPage extends Page with WebBrowserDSL {
  val conf = ConfigFactory.load()

  final val address = s"""$applicationContext${conf.getString("start.page")}}"""
  final override val title: String = "Sell a vehicle into the motor trade"
  final val titleCy: String = "Gwerthu cerbyd i'r fasnach foduron"

  override def url: String = WebDriverFactory.testUrl + address.substring(1)

  def startNow(implicit driver: WebDriver): Element = find(id(NextId)).get
}