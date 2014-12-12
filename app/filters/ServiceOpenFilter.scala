package filters

import uk.gov.dvla.vehicles.presentation.common.filters.{DateTimeZoneService, EnsureServiceOpenFilter}
import utils.helpers.Config
import com.google.inject.Inject

class ServiceOpenFilter @Inject()(implicit config: Config,
                                  timeZone: DateTimeZoneService) extends EnsureServiceOpenFilter {
  protected lazy val opening = config.opening
  protected lazy val closing = config.closing
  protected lazy val dateTimeZone = timeZone
  protected lazy val html = views.html.disposal_of_vehicle.closed("", "")

  override protected def html(opening: String, closing: String) = views.html.disposal_of_vehicle.closed(opening, closing)
}
