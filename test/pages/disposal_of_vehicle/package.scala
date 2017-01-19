package pages

import uk.gov.dvla.vehicles.presentation.common.testhelpers.ApplicationContext
import ApplicationContext.ApplicationRoot

package object disposal_of_vehicle {
  final implicit val applicationContext: ApplicationRoot = "/sell-to-the-trade"
  def buildAppUrl(urlPart: String) = ApplicationContext.buildAppUrl(urlPart)
}
