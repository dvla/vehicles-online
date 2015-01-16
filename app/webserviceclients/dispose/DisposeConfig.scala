package webserviceclients.dispose

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getOptionalProperty
import scala.concurrent.duration.DurationInt

class DisposeConfig {
  lazy val baseUrl = getOptionalProperty[String]("disposeVehicle.baseUrl").getOrElse("")
  lazy val requestTimeout = getOptionalProperty[Int]("disposeVehicle.requestTimeout").getOrElse(5.seconds.toMillis.toInt)
}
