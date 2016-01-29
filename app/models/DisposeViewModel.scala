package models

import uk.gov.dvla.vehicles.presentation.common.model.{TraderDetailsModel, VehicleAndKeeperDetailsModel}

final case class DisposeViewModel(vehicleDetails: VehicleAndKeeperDetailsModel,
                                  traderDetails: TraderDetailsModel,
                                  transactionId: Option[String] = None)
