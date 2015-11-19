package utils.helpers

import com.google.inject.Inject
import com.google.inject.name.Named
import controllers.routes
import play.api.mvc.Results.Redirect
import play.api.mvc.RequestHeader
import play.api.LoggerLike
import uk.gov.dvla.vehicles.presentation.common.ErrorStrategyBase
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter
import AccessLoggingFilter.AccessLoggerName
import uk.gov.dvla.vehicles.presentation.common.filters.ClfEntryBuilder
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.utils.helpers.CookieHelper

class ErrorStrategy @Inject()(clfEntryBuilder: ClfEntryBuilder,
                              @Named(AccessLoggerName) accessLogger: LoggerLike,
                              dateService: DateService)
  extends ErrorStrategyBase(clfEntryBuilder, clfEntry => accessLogger.info(clfEntry), accessLogger, dateService) {

    protected override def sessionExceptionResult(request: RequestHeader ) =
      CookieHelper.discardAllCookies(routes.BeforeYouStart.present)(request)

    protected  override def errorPageResult(exceptionDigest: String) =
      Redirect(routes.Error.present(exceptionDigest))
}