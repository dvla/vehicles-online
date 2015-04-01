package models

final case class DisposeViewModel(vehicleMake: Option[String],
                                  vehicleModel: Option[String],
                                  dealerName: String,
                                  dealerAddress: Seq[String],
                                  transactionId: Option[String] = None,
                                  registrationNumber: String)
