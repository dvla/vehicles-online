package conf

import helpers.UnitSpec

class MessageFilesSpec extends UnitSpec {

  def extractMessageKeys(file: String): List[String] = {
    val source = scala.io.Source.fromFile(file)
    val lines: List[String] = source.getLines().filterNot(_.isEmpty).filterNot(_.startsWith("#")).toList
    val keys: List[String] = lines.map(keyValue => keyValue.split("=").head.trim)
    source.close()
    keys
  }

  "Message files" should {
    val englishKeys = extractMessageKeys("conf/messages.en")
    val welshKeys = extractMessageKeys("conf/messages.cy")

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
  }
}
