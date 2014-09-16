package pages.common

import helpers.webbrowser.{Element, WebBrowserDSL}
import views.common.Feedback.FeedbackId
import org.openqa.selenium.WebDriver

object Feedback extends WebBrowserDSL {
  def mailto(implicit driver: WebDriver): Element = find(id(FeedbackId)).get
}
