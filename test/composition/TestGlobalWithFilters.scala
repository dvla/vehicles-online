package composition

import play.api.GlobalSettings
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.GlobalCreator

object TestGlobalWithFilters extends GlobalWithFilters with TestComposition

trait DisposeGlobalCreator extends GlobalCreator {
  override def global: GlobalSettings = TestGlobalWithFilters
}