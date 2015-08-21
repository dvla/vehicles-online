package webserviceclients.fakes

import play.api.libs.json.JsValue
import play.api.libs.ws.{WSCookie, WSResponse}
import scala.xml.Elem

final class FakeResponse(override val status: Int,
                         override val statusText: String = "",
                         headers: Map[String, String] = Map.empty,
                         fakeBody: Option[String] = None,
                         fakeXml: Option[Elem] = None,
                         fakeJson: Option[JsValue] = None) extends WSResponse {

  def allHeaders: Map[String, Seq[String]] = headers.map { case (k, v) => k -> Seq(v) }
  def underlying[T]: T = ???
  def cookies: Seq[WSCookie] = Nil

  def cookie(name: String): Option[WSCookie] = None

  override def header(key: String): Option[String] = headers.get(key)

  override lazy val body: String = fakeBody.getOrElse("")
  override lazy val xml: Elem = fakeXml.get
  // Beware that if you constructed with the default then calling this will throw an exception.
  override lazy val json: JsValue = fakeJson.get
}

object FakeResponse {
  def apply(status: Int,
            statusText: String = "",
            headers: Map[String, String] = Map.empty,
            fakeBody: Option[String] = None,
            fakeXml: Option[Elem] = None,
            fakeJson: Option[JsValue] = None) =
    new FakeResponse(status, statusText, headers, fakeBody, fakeXml, fakeJson)
}

