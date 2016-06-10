package pages

import composition.TestConfig

object ApplicationContext {

  def applicationContext: String =  try {
    import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.{getOptionalProperty, stringProp}
    getOptionalProperty[String]("application.context").getOrElse(TestConfig.DEFAULT_APPLICATION_CONTEXT)
  } catch {
    case _: Throwable => ""
  }
}
