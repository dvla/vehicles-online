package uk.gov.dvla.assign

import io.gatling.core.Predef._
import uk.gov.dvla.Helper.httpConf
import uk.gov.dvla.retention.Scenarios._

class Simulate extends Simulation {

  private val oneUser = atOnceUsers(1)

  setUp(
    // Happy paths
    assetsAreAccessible.inject(oneUser)
  ).
    protocols(httpConf).
    assertions(global.failedRequests.count.is(0))
}
