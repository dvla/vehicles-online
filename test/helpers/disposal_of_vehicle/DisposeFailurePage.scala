package helpers.disposal_of_vehicle

object DisposeFailurePage {
  val url = "/disposal-of-vehicle/dispose-failure"
  val title = "Dispose a vehicle into the motor trade: failure"

  def cacheSetupHappyPath () {
    SetUpTradeDetailsPage.setupCache()
    BusinessChooseYourAddressPage.setupCache()
    VehicleLookupPage.setupVehicleDetailsModelCache()
    DisposePage.setupDisposeFormModelCache()
    DisposePage.setupDisposeTransactionIdCache
  }
}
