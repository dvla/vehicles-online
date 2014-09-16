package pages.common

import helpers.webbrowser.{Element, WebBrowserDSL}
import views.common.Help
import Help.HelpLinkId
import org.openqa.selenium.WebDriver

object HelpPanel extends WebBrowserDSL {
  def help(implicit driver: WebDriver): Element = find(id(HelpLinkId)).get
}