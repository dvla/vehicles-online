package webserviceclients.dispose_service

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.{getOptionalProperty, stringProp, intProp}
import webserviceclients.dispose.DisposeConfig
import scala.concurrent.duration.DurationInt

/**
 * Fake Configuration that should be used in all the tests
 */
class FakeDisposeConfig extends DisposeConfig {
  override lazy val baseUrl = getOptionalProperty[String]("disposeVehicle.baseUrl").getOrElse("")
  override lazy val requestTimeout = getOptionalProperty[Int]("disposeVehicle.requestTimeout").getOrElse(5.seconds.toMillis.toInt)
}
