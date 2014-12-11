package pages.common

import uk.gov.dvla.vehicles.presentation.common.controllers.AlternateLanguages.{CyId, EnId}
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Element, WebBrowserDSL}
import org.openqa.selenium.WebDriver

object AlternateLanguages extends WebBrowserDSL {
  def cymraeg(implicit driver: WebDriver): Element = find(id(CyId)).get
  def english(implicit driver: WebDriver): Element = find(id(EnId)).get
  def isCymraegDisplayed(implicit driver: WebDriver): Boolean = find(id(CyId)).isDefined
  def isEnglishDisplayed(implicit driver: WebDriver): Boolean = find(id(EnId)).isDefined
}