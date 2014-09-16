package utils.helpers

import helpers.webbrowser.TestGlobal
import helpers.{UnitSpec, WithApplication}
import play.api.test.FakeApplication
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.AesEncryption

final class AesEncryptionSpec extends UnitSpec {
  "encryptCookie" should {
    "return an encrypted string" in new WithApplication(app = fakeAppWithCryptoConfig) {
      val aesEncryption = new AesEncryption
      aesEncryption.encrypt(ClearText) should not equal ClearText
    }

    "return a different encrypted string given same clear text input" in new WithApplication(app = fakeAppWithCryptoConfig) {
      val aesEncryption = new AesEncryption
      val cipherText1 = aesEncryption.encrypt(ClearText)
      val cipherText2 = aesEncryption.encrypt(ClearText)

      withClue("Initialization vectors must be used to ensure output is always random") {
        cipherText1 should not equal cipherText2
      }
    }

    "throw an exception when the application secret key is the wrong size" in new WithApplication(app = fakeAppWithWrongLengthAppSecretConfig) {
      intercept[Exception] {
        val aesEncryption = new AesEncryption
        aesEncryption.encrypt(ClearText)
      }
    }
  }

  "decryptCookie" should {
    "return a decrypted string" in new WithApplication(app = fakeAppWithCryptoConfig) {
      val aesEncryption = new AesEncryption
      val encrypted = aesEncryption.encrypt(ClearText)
      aesEncryption.decrypt(encrypted) should equal(ClearText)
    }
  }

  private val ClearText = "qwerty"
  private val fakeAppWithCryptoConfig = FakeApplication(
    withGlobal = Some(TestGlobal),
    additionalConfiguration = Map("application.secret256Bit" -> "MnPSvGpiEF5OJRG3xLAnsfmdMTLr6wpmJmZLv2RB9Vo=")
  )
  private val fakeAppWithWrongLengthAppSecretConfig = FakeApplication(
    withGlobal = Some(TestGlobal),
    additionalConfiguration = Map("application.secret256Bit" -> "rubbish")
  )
}