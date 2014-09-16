package webserviceclients.dispose

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getProperty
import scala.concurrent.duration.DurationInt

class DisposeConfig {
  val baseUrl = getProperty("disposeVehicle.baseUrl", "NOT FOUND")
  val requestTimeout = getProperty("disposeVehicle.requestTimeout", 5.seconds.toMillis.toInt)
}
