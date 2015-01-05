package uk.gov.dvla.vehicles.dispose.gatling

import io.gatling.core.Predef._
import Scenarios.dispose_start_to_finish_exit_on_fail
import Helper.httpConf

class DisposeSimulation extends Simulation {

  private def disposeToTradeTests = dispose_start_to_finish_exit_on_fail.inject(atOnceUsers(1))

  //setUp(
  //  Scenarios.dispose_ten_vehicles_using_caching.inject(
  //    atOnceUsers(1))
  //).protocols(httpConf)

  //setUp(
  //  Scenarios.dispose_vehicles_using_caching_over_10_min.inject(
  //    atOnceUsers(1))
  //).protocols(httpConf)

  /*
   * Complicated example...
   *
  setUp(
    scn.inject(nothingFor(4 seconds),
      atOnce(10 users),
      ramp(10 users) over (5 seconds),
      constantRate(20 usersPerSec) during (15 seconds),
      rampRate(10 usersPerSec) to(20 usersPerSec) during(10 minutes),
      split(1000 users).into(ramp(10 users) over (10 seconds))
                       .separatedBy(10 seconds),
      split(1000 users).into(ramp(10 users) over (10 seconds))
                       .separatedBy(atOnce(30 users)))
      .protocols(httpConf)
   */

  setUp(disposeToTradeTests).
    protocols(httpConf).
    assertions(global.failedRequests.count.is(0))
}
