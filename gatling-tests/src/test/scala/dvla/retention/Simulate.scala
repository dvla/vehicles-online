package uk.gov.dvla.retention

import io.gatling.core.Predef._
import uk.gov.dvla.Helper.httpConf
import uk.gov.dvla.retention.Scenarios._

class Simulate extends Simulation {

  private val oneUser = atOnceUsers(1)

  setUp(
    // Happy paths
    assetsAreAccessible.inject(oneUser),
//    registeredKeeperAndFullKeeperAddress.inject(oneUser),
//    registeredKeeperAndMakeNoModel.inject(oneUser),
//    registeredKeeperAndModelNoMake.inject(oneUser),
//    registeredKeeperAndPartialKeeperAddress.inject(oneUser),
    registeredKeeperVeryLongMakeAndModel.inject(oneUser),

    // The following test is currently failing due to a mod_security issue
    // https://github.com/SpiderLabs/ModSecurity/issues/582
    // It is commented to allow for a green pipeline but should be
    // uncommented when the above mod security problem is addressed.
    //
    // notRegisteredKeeperAndFullKeeperAddress.inject(oneUser),

    // Sad paths
    eligibilityCheckDirectToPaper.inject(oneUser),

    notEligibleToTransact.inject(oneUser),
    vrmNotFound.inject(oneUser)
  ).
    protocols(httpConf).
    assertions(global.failedRequests.count.is(0))
}
