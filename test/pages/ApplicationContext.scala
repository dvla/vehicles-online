package pages

object ApplicationContext {

  def applicationContext: String =  try {
    import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.{getOptionalProperty, stringProp}
    getOptionalProperty[String]("application.context").getOrElse("")
  } catch {
    case _: Throwable => ""
  }
}
