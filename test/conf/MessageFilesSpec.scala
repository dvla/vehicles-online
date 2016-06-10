package conf

import helpers.UnitSpec
import uk.gov.dvla.vehicles.presentation.common.testhelpers.MessageFilesSpecHelper.messagesFilesHelper
import uk.gov.dvla.vehicles.presentation.common.testhelpers.MessageFilesSpecHelper.ENGLISH_FILE
import uk.gov.dvla.vehicles.presentation.common.testhelpers.MessageFilesSpecHelper.WELSH_FILE

class MessageFilesSpec extends UnitSpec {

  val englishKeys = messagesFilesHelper.extractMessageKeys(ENGLISH_FILE)
  val welshKeys = messagesFilesHelper.extractMessageKeys(WELSH_FILE)
  val mapEnglish = messagesFilesHelper.extractMessageMap(ENGLISH_FILE)
  val mapWelsh = messagesFilesHelper.extractMessageMap(WELSH_FILE)

  "Message files" should {
    "have a corresponding Welsh key for each English key" in {
      val englishKeysWithNoWelshEquivalent = englishKeys.filterNot(enKey => welshKeys.contains(enKey))
      println(s"English keys that are missing in the Welsh message file: $englishKeysWithNoWelshEquivalent")
      englishKeysWithNoWelshEquivalent should equal(List.empty)
    }

    "have a corresponding English key for each Welsh key" in {
      val welshKeysWithNoEnglishEquivalent = welshKeys.filterNot(cyKey => englishKeys.contains(cyKey))
      println(s"Welsh keys that are missing in the English message file: $welshKeysWithNoEnglishEquivalent")
      welshKeysWithNoEnglishEquivalent should equal(List.empty)
    }

    "have an English value and a corresponding non-blank Welsh value" in {
      messagesFilesHelper.getBlankNonBlankValuesCount(mapEnglish, mapWelsh) should equal(0)
    }

    "have a Welsh value and a corresponding non-blank English value" in {
      messagesFilesHelper.getBlankNonBlankValuesCount(mapWelsh, mapEnglish) should equal(0)
    }

    "have no blank Welsh and English values" in {
      messagesFilesHelper.getBlankBlankValuesCount(mapWelsh, mapEnglish) should equal(0)
    }
  }
}
