package pages

object ApplicationContext {

  def applicationContext: String =  {
    import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getProperty
    getProperty[String]("application.context")
  }
}
