package pages

import composition.TestConfig
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.{getOptionalProperty, stringProp}
import uk.gov.dvla.vehicles.presentation.common.testhelpers.ApplicationContext
import ApplicationContext.ApplicationRoot

package object disposal_of_vehicle {
  final implicit val applicationContext: ApplicationRoot = try {
    getOptionalProperty[String]("application.context").getOrElse(TestConfig.DEFAULT_APPLICATION_CONTEXT)
  } catch {
    // Running the acceptance tests will cause "There is no started application" exception
    case _: Throwable => TestConfig.DEFAULT_APPLICATION_CONTEXT
  }

  def buildAppUrl(urlPart: String) = ApplicationContext.buildAppUrl(urlPart)
}
