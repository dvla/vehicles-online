package uk.gov.dvla.vehicles.acquire.gatling

import io.gatling.core.Predef._
import uk.gov.dvla.vehicles.acquire.gatling.Scenarios.beforeYouStart
import uk.gov.dvla.vehicles.dispose.gatling.Helper.httpConf

class AcquireSimulation extends Simulation {

  private def simulateWithOneUser = beforeYouStart.inject(atOnceUsers(1))

  setUp(simulateWithOneUser).
    protocols(httpConf).
    assertions(global.failedRequests.count.is(0))
}
