package models

final case class DisposeViewModel(vehicleMake: String,
                                  vehicleModel: String,
                                  dealerName: String,
                                  dealerAddress: Seq[String],
                                  transactionId: Option[String] = None,
                                  registrationNumber: String)
