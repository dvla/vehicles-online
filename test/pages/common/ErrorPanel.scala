package pages.common

import org.openqa.selenium.{By, WebDriver}
import org.scalatest.selenium.WebBrowser
import WebBrowser.find
import WebBrowser.id

object ErrorPanel {
  def numberOfErrors(implicit driver: WebDriver): Int =
    driver.findElement(By.cssSelector(".validation-summary")).findElements(By.tagName("li")).size

  def text(implicit driver: WebDriver): String =
    driver.findElement(By.cssSelector(".validation-summary")).getText

  def hasErrors(implicit driver: WebDriver): Boolean = find(id("validation-summary")).isDefined
}