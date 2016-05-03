package helpers

import composition.TestComposition
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Second, Span}
import uk.gov.dvla.vehicles.presentation.common.testhelpers.UnitTestHelper

abstract class UnitSpec extends UnitTestHelper with TestComposition {

  protected val timeout = Timeout(Span(1, Second))
}
