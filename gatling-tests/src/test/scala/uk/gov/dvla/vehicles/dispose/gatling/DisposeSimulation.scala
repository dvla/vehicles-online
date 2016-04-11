package uk.gov.dvla.vehicles.dispose.gatling

import io.gatling.core.Predef._
import Scenarios.dispose_start_to_finish_exit_on_fail
import Helper.httpConf
import scala.concurrent.duration._
import scala.language.postfixOps  // this avoids compiler warnings

class DisposeSimulation extends Simulation {

  private def disposeToTradeTests = dispose_start_to_finish_exit_on_fail.inject(atOnceUsers(1))

//  setUp(
//    Scenarios.dispose_ten_vehicles_using_caching.inject(
//      atOnceUsers(1))
//  ).protocols(httpConf)

//  setUp(
//    Scenarios.dispose_vehicles_using_caching_over_10_min.inject(
//      atOnceUsers(1))
//  ).protocols(httpConf)

  /*
   * Complicated example...
   */
/* TODO reduce the time this setup takes execute and identify why some failures are observed e.g.

  ---- Errors --------------------------------------------------------------------
> regex(Complete and confirm).find(0).exists, found nothing        2285 (91.40%)
> java.util.concurrent.TimeoutException: Request timed out to lo    112 ( 4.48%)
calhost/127.0.0.1:17443 of 60000 ms
> No attribute named 'csrf_prevention_token' is defined             100 ( 4.00%)
> regex(Buying a vehicle into trade).find(0).exists, found nothi      3 ( 0.12%)
ng
================================================================================
*/
/*
  val scn = Scenarios.dispose_start_to_finish_exit_on_fail
  setUp(
    scn.inject(nothingFor(4 seconds),
      atOnceUsers(10),
      rampUsers(10) over (5 seconds),
      constantUsersPerSec(20) during (15 seconds),
      rampUsersPerSec(10) to (20) during(10 minutes),
      splitUsers(1000).into(rampUsers(10) over (10 seconds))
                       .separatedBy(10 seconds),
      splitUsers(1000).into(rampUsers(10) over (10 seconds))
                       .separatedBy(atOnceUsers(30)))
      .protocols(httpConf)
  )
*/
  setUp(disposeToTradeTests).
    protocols(httpConf).
    assertions(global.failedRequests.count.is(0))
}
