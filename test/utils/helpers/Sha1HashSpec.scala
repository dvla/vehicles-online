package utils.helpers

import helpers.{UnitSpec, TestWithApplication}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.Sha1HashGenerator

final class Sha1HashSpec extends UnitSpec {

  "Sha1Hash" should {
    "return a hashed string" in new TestWithApplication {
      sha1Hash.hash(ClearText) should not equal ClearText
    }

    "returns the same hash repeatedly" in new TestWithApplication {
      val first = sha1Hash.hash(ClearText)
      val second = sha1Hash.hash(ClearText)
      first should equal(second)
    }

    "return expected length for the digest" in new TestWithApplication {
      sha1Hash.digestStringLength should equal(40)
    }
  }

  private val sha1Hash = new Sha1HashGenerator // Sharing immutable fixture objects via instance variables
  private val ClearText = "qwerty"
}