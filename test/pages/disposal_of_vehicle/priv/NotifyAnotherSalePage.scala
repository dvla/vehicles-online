package pages.disposal_of_vehicle.priv

import models.priv.NotifyAnotherSaleFormModel.Form.NotifyAnotherSaleId
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.{id, Element, find, radioButton, RadioButton}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.disposal_of_vehicle.priv.NotifyAnotherSale.NextId

object NotifyAnotherSalePage extends Page {
  final val address = pages.disposal_of_vehicle.buildAppUrl("private/notify-another-sale")
  final override val title: String = "Have you sold the vehicle to the same motor trader?"
  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  def yes(implicit driver: WebDriver): RadioButton =
    radioButton(id(s"${NotifyAnotherSaleId}_same_trader"))

  def no(implicit driver: WebDriver): RadioButton =
    radioButton(id(s"${NotifyAnotherSaleId}_new_trader"))

  def next(implicit driver: WebDriver): Element = find(id(NextId)).get
}
