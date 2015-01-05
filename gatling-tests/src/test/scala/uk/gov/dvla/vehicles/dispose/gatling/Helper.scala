package uk.gov.dvla.vehicles.dispose.gatling

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef.http

object Helper {

  private val config = ConfigFactory.load()

  def baseUrl: String =
    if (config.hasPath("test.url")) config.getString("test.url")
    else "http://localhost:9000"

  val httpConf = http
    .baseURL(Helper.baseUrl)
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-gb,en;q=0.5")
    .connection("keep-alive")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:26.0) Gecko/20100101 Firefox/26.0")
  //  .proxy(Proxy("localhost", 8081).httpsPort(8081))
}
