package uk.gov.dvla.vehicles.assign.gatling

import io.gatling.core.Predef._
import io.gatling.core.feeder.RecordSeqFeederBuilder
import io.gatling.http.Predef._

class Chains(data: RecordSeqFeederBuilder[String]) {

  def assetsAreAccessible =
    exec(
      http("screen.min.css")
        .get( s"""/assets/screen.min.css""")
        .headers(Map(
        """If-Modified-Since""" -> """Thu, 05 Jun 2014 21:08:06 GMT""",
        """If-None-Match""" -> """59f34576dba4629e6e960e1d514fe573775e9999"""))
    )
      .exec(
        http("print.min.css")
          .get( s"""/assets/print.min.css""")
          .headers(Map(
          """If-Modified-Since""" -> """Thu, 05 Jun 2014 21:08:08 GMT""",
          """If-None-Match""" -> """b2b112249c52769ac41acd83e388f550e4c39c6f"""))
      )
      .exec(
        http("require.js")
          .get( s"""/assets/javascripts/require.js""")
          .headers(Map(
          """Accept""" -> """*/*""",
          """If-Modified-Since""" -> """Tue, 06 Aug 2013 09:49:32 GMT""",
          """If-None-Match""" -> """858bab5a8e8f73a1d706221ed772a4f740e168d5"""))
      )
      .exec(
        http("govuk-crest.png")
          .get( s"""/assets/lib/vehicles-presentation-common/images/govuk-crest.png""")
          .headers(Map(
          """Accept""" -> """image/png,image/*;q=0.8,*/*;q=0.5""",
          """If-Modified-Since""" -> """Thu, 22 May 2014 14:25:18 GMT""",
          """If-None-Match""" -> """0464ba08d53d88645ca77f9907c082c8c10d563b"""))
      )
      .exec(
        http("main.js")
          .get( s"""/assets/javascripts/main.js""")
          .headers(Map(
          """Accept""" -> """*/*""",
          """If-Modified-Since""" -> """Thu, 05 Jun 2014 21:10:42 GMT""",
          """If-None-Match""" -> """5f859f72e7cc426915cf32f2643ee5fc494b04a8"""))
      )
}
