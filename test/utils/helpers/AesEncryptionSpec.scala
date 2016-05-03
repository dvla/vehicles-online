package utils.helpers

import composition.TestGlobalWithFilters
import helpers.{UnitSpec, TestWithApplication}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.AesEncryption
import uk.gov.dvla.vehicles.presentation.common.testhelpers.LightFakeApplication

final class AesEncryptionSpec extends UnitSpec {
  "encryptCookie" should {
    "return an encrypted string" in new TestWithApplication(fakeAppWithCryptoConfig) {
      val aesEncryption = new AesEncryption
      aesEncryption.encrypt(ClearText) should not equal ClearText
    }

    "return a different encrypted string given same clear text input" in
      new TestWithApplication(fakeAppWithCryptoConfig) {
      val aesEncryption = new AesEncryption
      val cipherText1 = aesEncryption.encrypt(ClearText)
      val cipherText2 = aesEncryption.encrypt(ClearText)

      withClue("Initialization vectors must be used to ensure output is always random") {
        cipherText1 should not equal cipherText2
      }
    }

    "throw an exception when the application secret key is the wrong size" in
      new TestWithApplication(fakeAppWithWrongLengthAppSecretConfig) {
      intercept[Exception] {
        val aesEncryption = new AesEncryption
        aesEncryption.encrypt(ClearText)
      }
    }
  }

  "decryptCookie" should {
    "return a decrypted string" in new TestWithApplication(fakeAppWithCryptoConfig) {
      val aesEncryption = new AesEncryption
      val encrypted = aesEncryption.encrypt(ClearText)
      aesEncryption.decrypt(encrypted) should equal(ClearText)
    }
  }

  private val ClearText = "qwerty"

  private val fakeAppWithCryptoConfig =
    LightFakeApplication(TestGlobalWithFilters,Map("application.secret256Bit" -> "MnPSvGpiEF5OJRG3xLAnsfmdMTLr6wpmJmZLv2RB9Vo="))

  private val fakeAppWithWrongLengthAppSecretConfig =
    LightFakeApplication(TestGlobalWithFilters,Map("application.secret256Bit" -> "rubbish="))

}