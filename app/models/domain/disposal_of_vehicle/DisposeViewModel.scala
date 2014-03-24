package models.domain.disposal_of_vehicle

case class DisposeViewModel(vehicleMake: String, vehicleModel: String, keeperName: String, keeperAddress: AddressViewModel, dealerName: String, dealerAddress: AddressViewModel, transactionId: Option[String] = None, registrationNumber: Option[String] = None)