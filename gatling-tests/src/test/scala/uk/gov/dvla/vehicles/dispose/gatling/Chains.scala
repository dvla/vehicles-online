package uk.gov.dvla.vehicles.dispose.gatling

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.dvla.vehicles.dispose.gatling.Headers.{headers_accept_html, headers_accept_png, headers_x_www_form_urlencoded}

object Chains {

  private val chain_assets_200 =
    exec(http("screen.min.css")
      .get(s"/assets/screen.min.css")
    )
      /*.exec(http("fonts.min.css")
        .get("""/assets/fonts.min.css""")
      )*/
      .exec(http("print.min.css")
      .get(s"/assets/print.min.css")
      )
      .exec(
        http("govuk-crest.png")
          .get(s"/assets/lib/vehicles-presentation-common/images/govuk-crest.png")
          .headers(Map( """Accept""" -> """image/png,image/*;q=0.8,*/*;q=0.5"""))
      )
      .exec(http("require.js")
      .get(s"/assets/javascripts/require.js")
      .headers(Map( """Accept""" -> """*/*"""))
      )
      .exec(http("custom.js")
      .get(s"/assets/javascripts/main.js")
      .headers(Map( """Accept""" -> """*/*"""))
      )

  private val chain_assets_304 =
    exec(http("screen.min.css")
      .get(s"/assets/screen.min.css")
      .headers(Map(
      """If-Modified-Since""" -> """Thu, 05 Jun 2014 21:08:06 GMT""",
      """If-None-Match""" -> """59f34576dba4629e6e960e1d514fe573775e9999"""))
      //.check(status.is(304))
    )
      /*.exec(http("fonts.min.css")
        .get("""/assets/fonts.min.css""")
        .headers(Map(
          """If-Modified-Since""" -> """Fri, 30 May 2014 13:08:32 GMT""",
          """If-None-Match""" -> """0702d8d00d43562d6fa1a4e87ac82609dc70ffc9"""))
        .check(status.is(304))
      )*/
      .exec(http("print.min.css")
      .get(s"/assets/print.min.css")
      .headers(Map(
      """If-Modified-Since""" -> """Thu, 05 Jun 2014 21:08:08 GMT""",
      """If-None-Match""" -> """b2b112249c52769ac41acd83e388f550e4c39c6f"""))
        //.check(status.is(304))
      )
      .exec(http("require.js")
      .get(s"/assets/javascripts/require.js")
      .headers(Map(
      """Accept""" -> """*/*""",
      """If-Modified-Since""" -> """Tue, 06 Aug 2013 09:49:32 GMT""",
      """If-None-Match""" -> """858bab5a8e8f73a1d706221ed772a4f740e168d5"""))
        //.check(status.is(304))
      )
      .exec(
        http("govuk-crest.png")
          .get(s"/assets/lib/vehicles-presentation-common/images/govuk-crest.png")
          .headers(Map(
          """Accept""" -> """image/png,image/*;q=0.8,*/*;q=0.5""",
          """If-Modified-Since""" -> """Thu, 22 May 2014 14:25:18 GMT""",
          """If-None-Match""" -> """0464ba08d53d88645ca77f9907c082c8c10d563b"""))
        //.check(status.is(304))
      )
      .exec(http("custom.js")
      .get(s"/assets/javascripts/main.js")
      .headers(Map(
      """Accept""" -> """*/*""",
      """If-Modified-Since""" -> """Thu, 05 Jun 2014 21:10:42 GMT""",
      """If-None-Match""" -> """5f859f72e7cc426915cf32f2643ee5fc494b04a8"""))
        //.check(status.is(304))
      )

  val setupTradeDetails = csv("data/setup-trade-details.csv").circular

  val chain_setup_trader_details =
    exec(http(s"GET /before-you-start")
      .get(s"/before-you-start")
      .headers(headers_accept_html)
      .check(regex( """Sell a vehicle into the motor trade""").exists)
    )
      .exec(chain_assets_200)
      .exec(http(s"GET /setup-trade-details")
      .get(s"/setup-trade-details")
      .headers(headers_accept_html)
      .check(regex( """Provide trader details""").exists)
      .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token")))
      .exec(chain_assets_304).feed(setupTradeDetails)
      .exec(http("POST /setup-trade-details")
      .post("/setup-trade-details")
      .headers(headers_x_www_form_urlencoded)
      .formParam( """traderName""", "${traderName}")
      .formParam( """traderPostcode""", "${traderPostcode}")
      .formParam( """csrf_prevention_token""", "${csrf_prevention_token}")
      .formParam( """action""", """""")
      .check(regex( """Select trader address""").exists)
      .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token")))
      .exec(chain_assets_304).exec(http("POST /business-choose-your-address")
      .post("/business-choose-your-address")
      .headers(headers_x_www_form_urlencoded)
//      .param( """disposal_businessChooseYourAddress_addressSelect""", "${uprn}") // Use UPRN
      .formParam( """disposal_businessChooseYourAddress_addressSelect""", "0") // UPRN disabled for Northern Ireland
      .formParam( """csrf_prevention_token""", "${csrf_prevention_token}")
      .formParam( """action""", """""")
      .check(regex( """Enter vehicle details""").exists)
      .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token")))
      .exec(chain_assets_304)
  val vehicleLookup = csv("data/vehicle-lookup.csv").circular
  val dispose = csv("data/dispose.csv").circular

  val chain_dispose_vehicle =
    feed(vehicleLookup)
      .exec(http(s"POST /vehicle-lookup")
      .post(s"/vehicle-lookup")
      .headers(headers_x_www_form_urlencoded)
      .formParam( """vehicleRegistrationNumber""", "${vehicleRegistrationNumber}")
      .formParam( """documentReferenceNumber""", "${documentReferenceNumber}")
      .formParam( """csrf_prevention_token""", "${csrf_prevention_token}")
      .formParam( """action""", """""")
      .check(regex( """Complete &amp; confirm""").exists)
      .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
      )
      .exec(chain_assets_304)
      .feed(dispose)
      .exec(http("POST /complete-and-confirm")
      .post("/complete-and-confirm")
      .headers(headers_x_www_form_urlencoded)
      .formParam( """mileage""", "${mileage}")
      .formParam( """consent""", "${consent}")
      .formParam( """lossOfRegistrationConsent""", "${lossOfRegistrationConsent}")
      .formParam( """dateOfDisposal.day""", "${day}")
      .formParam( """dateOfDisposal.month""", "${month}")
      .formParam( """dateOfDisposal.year""", "${year}")
      .formParam( """csrf_prevention_token""", "${csrf_prevention_token}")
      .formParam( """action""", """""")
      .check(regex( """Summary""").exists)
      .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
      )
      /*
      .exec(http("GET /sell-to-the-trade-success")
        .get("/sell-to-the-trade-success")
        .headers(headers_accept_html)
        .check(regex("""Sell a vehicle into the motor trade: summary""").exists)
      )*/
      .exec(chain_assets_304)
      .exec(http("icon-tick-green.gif")
      .get(s"/assets/lib/vehicles-presentation-common/images/icon-tick-green.gif")
      .headers(headers_accept_png)
      )

  val chain_new_dispose =
    exec(http("POST /vehicle-lookup")
      .post("/vehicle-lookup")
      .headers(headers_accept_html)
      .formParam( """csrf_prevention_token""", "${csrf_prevention_token}")
      .formParam( """action""", """""")
      .check(regex( """Find vehicle details""").exists)
    )
      .exec(chain_assets_304)

  val chain_exit_service =
    exec(http("POST /sell-to-the-trade-success/exit")
      .post("/sell-to-the-trade-success/exit")
      .headers(headers_accept_html)
      .formParam( """csrf_prevention_token""", "${csrf_prevention_token}")
      .formParam( """action""", """""")
      .check(regex( """Sell a vehicle into the motor trade""").exists)
    )
      .exec(chain_assets_304)
}
