package filters

import com.google.inject.Inject
import controllers.BusinessController
import uk.gov.dvla.vehicles.presentation.common.filters.{DateTimeZoneService, EnsureServiceOpenFilter}
import utils.helpers.Config

class ServiceOpenFilter @Inject()(implicit config: Config,
                                  timeZone: DateTimeZoneService)
  extends EnsureServiceOpenFilter with BusinessController {

  protected lazy val opening = config.openingTimeMinOfDay
  protected lazy val closing = config.closingTimeMinOfDay
  protected lazy val dateTimeZone = timeZone
  protected lazy val html = views.html.disposal_of_vehicle.closed("", "")

  override protected def html(opening: String, closing: String) = views.html.disposal_of_vehicle.closed(opening, closing)
}
