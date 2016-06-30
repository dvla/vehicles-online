package utils.helpers

import com.google.inject.Inject
import com.google.inject.name.Named
import controllers.routes
import play.api.LoggerLike
import uk.gov.dvla.vehicles.presentation.common.ErrorStrategyBase
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter.AccessLoggerName
import uk.gov.dvla.vehicles.presentation.common.filters.ClfEntryBuilder
import uk.gov.dvla.vehicles.presentation.common.services.DateService

class ErrorStrategy @Inject()(clfEntryBuilder: ClfEntryBuilder,
                              @Named(AccessLoggerName) accessLogger: LoggerLike,
                              dateService: DateService,
                              exceptionDigest: String)
  extends ErrorStrategyBase(clfEntryBuilder, clfEntry => accessLogger.info(clfEntry), accessLogger, dateService,
    routes.BeforeYouStart.present, routes.Error.present(exceptionDigest))

