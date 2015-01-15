package pages

object ApplicationContext {

  def applicationContext: String =  try {
    import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getProperty
    getProperty[String]("application.context")
  } catch {
    case _: Throwable => ""
  }
}
