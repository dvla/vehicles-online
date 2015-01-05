package uk.gov.dvla.vehicles.acquire.gatling

import io.gatling.core.Predef._

object Scenarios {

  def beforeYouStart = {
    val data = csv("acquire/data/happy/BeforeYouStart.csv").circular
    val chain = new Chains(data)
    endToEnd(
      scenarioName = "A dummy gatling trip through the pages. Will change as more pages are developed",
      chain
    )
  }

  private def endToEnd(scenarioName: String, chain: Chains) =
    scenario(scenarioName)
      .exitBlockOnFail(
        exec(
          chain.beforeYouStart,
          chain.beforeYouStartToProvideTradeDetails
        )
      )
}
