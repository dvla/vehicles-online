package pages.common

import org.openqa.selenium.WebDriver
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Element, WebBrowserDSL}
import views.common.ProtoType.FeedbackId

object Feedback extends WebBrowserDSL {
  def mailto(implicit driver: WebDriver): Element = find(id(FeedbackId)).get
}
