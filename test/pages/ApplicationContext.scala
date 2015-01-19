package pages

object ApplicationContext {

  def applicationContext: String =  try {
    import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getOptionalProperty
    getOptionalProperty[String]("application.context").getOrElse("")
  } catch {
    case _: Throwable => ""
  }
}
