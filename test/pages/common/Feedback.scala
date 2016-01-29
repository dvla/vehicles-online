package pages.common

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser
import WebBrowser.find
import WebBrowser.id
import WebBrowser.Element
import views.common.ProtoType.FeedbackId

object Feedback {
  def mailto(implicit driver: WebDriver): Element = find(id(FeedbackId)).get

  final val FeedbackLink = s"""<a id="${views.common.ProtoType.FeedbackId}" href="${controllers.routes.FeedbackController.present()}" target="_blank">"""
}
