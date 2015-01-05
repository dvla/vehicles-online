package uk.gov.dvla.vehicles.acquire.gatling

import io.gatling.core.Predef._
import io.gatling.core.feeder.RecordSeqFeederBuilder
import io.gatling.http.Predef._
import uk.gov.dvla.vehicles.dispose.gatling.Headers
import uk.gov.dvla.vehicles.dispose.gatling.Headers.headers_accept_html

class Chains(data: RecordSeqFeederBuilder[String]) {

  private val beforeYouStartPageTitle = "Buy a vehicle from the motor trade"
  private val setupTradeDetailsPageTitle = "Provide trader details"

  private val assets200 =
    exec(
      http("screen.min.css")
        .get( s"""/assets/screen.min.css""")
    )
      /*.exec(
      http(s"GET /assets/fonts.min.css")
        .get(s"""/assets/fonts.min.css""")
      )*/
      .exec(
        http("print.min.css")
          .get( s"""/assets/print.min.css""")
      )
      .exec(
        http("govuk-crest.png")
          .get( s"""/assets/lib/vehicles-presentation-common/images/govuk-crest.png""")
          .headers(Map( """Accept""" -> """image/png,image/*;q=0.8,*/*;q=0.5"""))
      )
      .exec(
        http("require.js")
          .get( s"""/assets/javascripts/require.js""")
          .headers(Map( """Accept""" -> """*/*"""))
      )
      .exec(
        http("main.js")
          .get( s"""/assets/javascripts/main.js""")
          .headers(Map( """Accept""" -> """*/*"""))
      )

  private val assets304 =
    exec(
      http("screen.min.css")
        .get( s"""/assets/screen.min.css""")
        .headers(Map(
        """If-Modified-Since""" -> """Thu, 05 Jun 2014 21:08:06 GMT""",
        """If-None-Match""" -> """59f34576dba4629e6e960e1d514fe573775e9999"""))
      //.check(status.is(304))
    )
      /*.exec(
      http("fonts.min.css")
        .get(s"""/assets/fonts.min.css""")
        .headers(Map(
          """If-Modified-Since""" -> """Fri, 30 May 2014 13:08:32 GMT""",
          """If-None-Match""" -> """0702d8d00d43562d6fa1a4e87ac82609dc70ffc9"""))
        .check(status.is(304))
      )*/
      .exec(
        http("print.min.css")
          .get( s"""/assets/print.min.css""")
          .headers(Map(
          """If-Modified-Since""" -> """Thu, 05 Jun 2014 21:08:08 GMT""",
          """If-None-Match""" -> """b2b112249c52769ac41acd83e388f550e4c39c6f"""))
        //.check(status.is(304))
      )
      .exec(
        http("require.js")
          .get( s"""/assets/javascripts/require.js""")
          .headers(Map(
          """Accept""" -> """*/*""",
          """If-Modified-Since""" -> """Tue, 06 Aug 2013 09:49:32 GMT""",
          """If-None-Match""" -> """858bab5a8e8f73a1d706221ed772a4f740e168d5"""))
        //.check(status.is(304))
      )
      .exec(
        http("govuk-crest.png")
          .get( s"""/assets/lib/vehicles-presentation-common/images/govuk-crest.png""")
          .headers(Map(
          """Accept""" -> """image/png,image/*;q=0.8,*/*;q=0.5""",
          """If-Modified-Since""" -> """Thu, 22 May 2014 14:25:18 GMT""",
          """If-None-Match""" -> """0464ba08d53d88645ca77f9907c082c8c10d563b"""))
        //.check(status.is(304))
      )
      .exec(
        http("main.js")
          .get( s"""/assets/javascripts/main.js""")
          .headers(Map(
          """Accept""" -> """*/*""",
          """If-Modified-Since""" -> """Thu, 05 Jun 2014 21:10:42 GMT""",
          """If-None-Match""" -> """5f859f72e7cc426915cf32f2643ee5fc494b04a8"""))
        //.check(status.is(304))
      )

  def beforeYouStart =
    exitBlockOnFail(
      exec(
        http("beforeYouStart")
          .get(s"/before-you-start")
          .headers(headers_accept_html)
          // Assertions
          .check(status.is(200))
          .check(regex(beforeYouStartPageTitle).exists) // Page title
      )
        .exec(assets200)
    )

  def beforeYouStartToProvideTradeDetails =
    exitBlockOnFail(
      exec(
        http("beforeYouStartToProvideTradeDetails")
          .get(s"/setup-trade-details") // Gatlin doesn't click on links, you need to go to the next page.
          .headers(headers_accept_html)
          .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
          // Assertions
          .check(regex(setupTradeDetailsPageTitle).exists) // Page title
      )
        .exec(assets304) // check the assets haven't changed
    )
}
