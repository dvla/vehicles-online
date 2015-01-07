package uk.gov.dvla.vehicles.dispose.gatling

import io.gatling.core.Predef._

import scala.concurrent.duration._
import Chains._

object Scenarios {

  val dispose_start_to_finish =
    scenario("Single vehicle disposal from start to finish")
      .exec(chain_setup_trader_details)
      .exec(chain_dispose_vehicle)
      .exec(chain_exit_service)

  val dispose_start_to_finish_exit_on_fail =
    scenario("Single vehicle disposal from start to finish")
      .exec(exitBlockOnFail(chain_setup_trader_details))
      .exec(exitBlockOnFail(chain_dispose_vehicle))
      .exec(exitBlockOnFail(chain_exit_service))

  val dispose_ten_vehicles_using_caching =
    scenario("Ten vehicle disposals using caching feature")
      .exec(chain_setup_trader_details)
      .exec(chain_dispose_vehicle)
      .repeat(10) {
        exec(chain_new_dispose)
        .exec(chain_dispose_vehicle)
      }
      .exec(chain_exit_service)

  val dispose_vehicles_using_caching_over_10_min =
    scenario("Multiple vehicle disposals using caching feature over ~10 min period")
      .exec(chain_setup_trader_details)
      .exec(chain_dispose_vehicle)
      .during(10 minutes) {
        exec(chain_new_dispose)
        .exec(chain_dispose_vehicle)
      }
      .exec(chain_exit_service)
}
