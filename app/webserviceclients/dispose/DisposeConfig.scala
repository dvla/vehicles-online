package webserviceclients.dispose

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getProperty

class DisposeConfig {
  lazy val baseUrl = getProperty[String]("disposeVehicle.baseUrl")
  lazy val requestTimeout = getProperty[Int]("disposeVehicle.requestTimeout")
}
