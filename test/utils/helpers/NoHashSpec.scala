package utils.helpers

import helpers.{UnitSpec, WithApplication}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.NoHashGenerator

final class NoHashSpec extends UnitSpec {

  "NoHash" should {
    "return a clear text string" in new WithApplication {
      noHash.hash(ClearText) should equal(ClearText)
    }

    "return expected length for the digest" in new WithApplication {
      noHash.digestStringLength should equal(0)
    }
  }

  private val noHash = new NoHashGenerator // Sharing immutable fixture objects via instance variables
  private val ClearText = "qwerty"
}