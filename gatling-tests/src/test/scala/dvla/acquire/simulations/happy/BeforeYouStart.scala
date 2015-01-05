package uk.gov.dvla.acquire.simulations.happy

import io.gatling.core.Predef._
import uk.gov.dvla.Helper.httpConf
import uk.gov.dvla.acquire.Scenarios.beforeYouStart

class BeforeYouStart extends Simulation {

  private def simulateWithOneUser = beforeYouStart.inject(atOnceUsers(1))

  setUp(simulateWithOneUser).
    protocols(httpConf).
    assertions(global.failedRequests.count.is(0))
}