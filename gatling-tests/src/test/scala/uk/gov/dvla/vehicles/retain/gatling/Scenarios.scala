package uk.gov.dvla.vehicles.retain.gatling

import io.gatling.core.Predef._
import io.gatling.core.feeder.{Record, RecordSeqFeederBuilder}

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

  def registeredKeeperAndFullKeeperAddress = {
    val data = csv("retention/data/happy/RegisteredKeeperAndFullKeeperAddress.csv").circular
    val chain = new Chains(data)
    endToEnd(
      scenarioName = "Single retention from start to finish with Registered Keeper And Full Keeper Address",
      chain
    )
  }

  def registeredKeeperAndPartialKeeperAddress = {
    val data = csv("retention/data/happy/RegisteredKeeperAndPartialKeeperAddress.csv").circular
    val chain = new Chains(data)
    endToEnd(
      scenarioName = "Single retention from start to finish with Registered Keeper And Partial Keeper Address",
      chain
    )
  }

  def registeredKeeperAndMakeNoModel = {
    val data = csv("retention/data/happy/RegisteredKeeperAndMakeNoModel.csv").circular
    val chain = new Chains(data)
    endToEnd(
      scenarioName = "Single retention from start to finish with Registered Keeper And make no model",
      chain
    )
  }

  def registeredKeeperAndModelNoMake = {
    val data = csv("retention/data/happy/RegisteredKeeperAndModelNoMake.csv").circular
    val chain = new Chains(data)
    endToEnd(
      scenarioName = "Single retention from start to finish with Registered Keeper And model no make",
      chain
    )
  }

  def registeredKeeperVeryLongMakeAndModel = {
    val data = csv("retention/data/happy/RegisteredKeeperVeryLongMakeAndModel.csv").circular
    val chain = new Chains(data)
    endToEnd(
      scenarioName = "Single retention from start to finish with Registered Keeper with very long make and model",
      chain
    )
  }

  def notRegisteredKeeperAndFullKeeperAddress = {
    val data = csv("retention/data/happy/NotRegisteredKeeperAndFullKeeperAddress.csv").circular
    val chain = new Chains(data)
    scenario("Single retention from start to finish with not Registered Keeper And Full Keeper Address")
      .exitBlockOnFail(
        exec(
          chain.beforeYouStart,
          chain.beforeYouStartToVehicleLookup,
          chain.vehicleLookupToSetupBusinessDetails,
          chain.setupBusinessDetailsToBusinessChooseYourAddress,
          chain.businessChooseYourAddressToConfirmBusiness,
//          chain.confirmToFullscreenSolvePayment
          chain.confirmBusinessToIframePayment,
          chain.paymentCallbackToRetainToSuccessPayment,
          chain.successPaymentToSuccess
        )
      )
  }

  // Sad paths
  def vrmNotFound = {
    val data = csv("retention/data/sad/VrmNotFound.csv").circular
    val chain = new Chains(data)
    scenario("Vrm not found")
      .exitBlockOnFail(
        exec(
          chain.beforeYouStart,
          chain.beforeYouStartToVehicleLookup,
          chain.vehicleLookupToVehicleLookupFailure
        )
      )
  }

  def eligibilityCheckDirectToPaper = {
    val data = csv("retention/data/sad/EligibilityCheckDirectToPaper.csv").circular
    val chain = new Chains(data)
    scenario("Eligibility Check - Direct to Paper")
      .exitBlockOnFail(
        exec(
          chain.beforeYouStart,
          chain.beforeYouStartToVehicleLookup,
          chain.vehicleLookupToDirectToPaper
        )
      )
  }

  def notEligibleToTransact = {
    val data = csv("retention/data/sad/NotEligibleToTransact.csv").circular
    val chain = new Chains(data)
    scenario("Eligibility Check - Not Eligible To Transact")
      .exitBlockOnFail(
        exec(
          chain.beforeYouStart,
          chain.beforeYouStartToVehicleLookup,
          chain.vehicleLookupToNotEligibleToTransact
        )
      )
  }

  private def endToEnd(scenarioName: String, chain: Chains) =
    scenario(scenarioName)
      .exitBlockOnFail(
        exec(
          chain.beforeYouStart,
          chain.beforeYouStartToVehicleLookup,
          chain.vehicleLookupToConfirm,
//          chain.confirmToFullscreenSolvePayment
          chain.confirmToIframePayment,
          chain.paymentCallbackToRetainToSuccessPayment,
          chain.successPaymentToSuccess
        )
      )
}
