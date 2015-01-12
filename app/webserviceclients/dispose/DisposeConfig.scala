package webserviceclients.dispose

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getProperty
import scala.concurrent.duration.DurationInt

class DisposeConfig {
  lazy val baseUrl = getProperty[String]("disposeVehicle.baseUrl")
  lazy val requestTimeout = getProperty[Int]("disposeVehicle.requestTimeout")//, 5.seconds.toMillis.toInt)
}
