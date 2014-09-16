package composition

import com.google.inject.Guice
import play.filters.gzip.GzipFilter
import uk.gov.dvla.vehicles.presentation.common.filters.{CsrfPreventionFilter, EnsureSessionCreatedFilter, AccessLoggingFilter}
import utils.helpers.ErrorStrategy
import filters.EnsureServiceOpenFilter

trait Composition {
  lazy val injector = Guice.createInjector(DevModule)

  lazy val filters = Array(
    injector.getInstance(classOf[EnsureSessionCreatedFilter]),
    new GzipFilter(),
    injector.getInstance(classOf[AccessLoggingFilter]),
    injector.getInstance(classOf[CsrfPreventionFilter]),
    injector.getInstance(classOf[EnsureServiceOpenFilter])
  )

  lazy val errorStrategy = injector.getInstance(classOf[ErrorStrategy])
}