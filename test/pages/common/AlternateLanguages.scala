package pages.common

import helpers.webbrowser.{Element, WebBrowserDSL}
import uk.gov.dvla.vehicles.presentation.common.controllers.AlternateLanguages.{CyId, EnId}
import org.openqa.selenium.WebDriver

object AlternateLanguages extends WebBrowserDSL {
  def cymraeg(implicit driver: WebDriver): Element = find(id(CyId)).get
  def english(implicit driver: WebDriver): Element = find(id(EnId)).get
  def hasCymraeg(implicit driver: WebDriver): Boolean = find(id(CyId)).isDefined
  def hasEnglish(implicit driver: WebDriver): Boolean = find(id(EnId)).isDefined
}