package uk.gov.dvla.vehicles.assign.gatling

import io.gatling.core.Predef._
import io.gatling.core.feeder._

object Scenarios {

  // Happy paths
  def assetsAreAccessible = {
    val data = RecordSeqFeederBuilder[String](records = IndexedSeq.empty[Record[String]])
    val chain = new Chains(data)
    scenario("Assets Are accessible")
      .exec(
        chain.assetsAreAccessible
      )
  }
}
