package utils.helpers

import com.google.inject.Inject
import com.google.inject.name.Named
import controllers.routes
import java.util.Date
import javax.crypto.BadPaddingException
import play.api.libs.Codecs
import play.api.mvc.Results.Redirect
import play.api.mvc.{RequestHeader, Result}
import play.api.{Logger, LoggerLike}
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter
import AccessLoggingFilter.AccessLoggerName
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.InvalidSessionException
import uk.gov.dvla.vehicles.presentation.common.filters.ClfEntryBuilder

import scala.concurrent.{ExecutionContext, Future}

class ErrorStrategy @Inject()(clfEntryBuilder: ClfEntryBuilder,
                              @Named(AccessLoggerName) accessLogger: LoggerLike) {

  def apply(request: RequestHeader, ex: Throwable)
           (implicit executionContext: ExecutionContext): Future[Result] = {
    val result = ex.getCause match {
      case _: BadPaddingException => CookieHelper.discardAllCookies(request)
      case _: InvalidSessionException => CookieHelper.discardAllCookies(request)
      case cause =>
        val exceptionDigest = Codecs.sha1(Option(cause).fold("")(c => Option(c.getMessage).getOrElse("")))
        Logger.error(s"Exception thrown with digest '$exceptionDigest'", cause)
        Future(Redirect(routes.Error.present(exceptionDigest)))
    }
    result.map { result =>
      accessLogger.info(clfEntryBuilder.clfEntry(new Date(), request, result))
    }
    result
  }
}