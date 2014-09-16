package helpers

import uk.gov.dvla.vehicles.presentation.common.views.helpers.HtmlArgsExtensions
import HtmlArgsExtensions.RichHtmlArgs

final class HtmlArgsExtensionsSpec extends UnitSpec {
  "HtmlArgsExtensions maxlength rules" should {
    "remove maxLength from args when key maxLength is present and in test mode" in new WithApplication {
      val richHtmlArgs = new RichHtmlArgs(htmlArgsWithMaxLength)

      val result = richHtmlArgs.maxLengthRules

      result should equal(htmlArgsMinimal)
    }

    "return the same args when key maxLength is present and in test mode" in new WithApplication {
      val richHtmlArgs = new RichHtmlArgs(htmlArgsWithMaxLength, validationOff = false)

      val result = richHtmlArgs.maxLengthRules

      result should equal(htmlArgsWithMaxLength)
    }

    "return the same args when key maxLength is not present and test mode" in new WithApplication {
      val richHtmlArgs = new RichHtmlArgs(htmlArgsMinimal)
      // Override validationOff to check the behaviour of the production code.
      val result = richHtmlArgs.maxLengthRules

      result should equal(htmlArgsMinimal)
    }

    "add key maxLength with default value to args not present and not in test mode" in new WithApplication {
      val richHtmlArgs = new RichHtmlArgs(htmlArgsMinimal, validationOff = false)
      // Override validationOff to check the behaviour of the production code.
      val result = richHtmlArgs.maxLengthRules

      result should equal(htmlArgsWithMaxLength)
    }
  }

  "HtmlArgsExtensions autocomplete rules" should {
    val htmlArgsMinimal: Map[Symbol, Any] = Map('title -> "test")
    val htmlArgsWithAutoCompleteOff: Map[Symbol, Any] = Map('title -> "test", 'autocomplete -> "off")
    val htmlArgsWithAutoCompleteOn: Map[Symbol, Any] = Map('title -> "test", 'autocomplete -> "on")

    "add autocomplete off when key is not present" in new WithApplication {
      val richHtmlArgs = new RichHtmlArgs(htmlArgsMinimal)

      val result = richHtmlArgs.autoCompleteRules

      result should equal(htmlArgsWithAutoCompleteOff)
    }

    "return the same args when key-value autocomplete 'off' is present" in new WithApplication {
      val richHtmlArgs = new RichHtmlArgs(htmlArgsWithAutoCompleteOff)

      val result = richHtmlArgs.autoCompleteRules

      result should equal(htmlArgsWithAutoCompleteOff)
    }

    "replace key-value autocomplete 'on' with autocomplete 'off'" in new WithApplication {
      val richHtmlArgs = new RichHtmlArgs(htmlArgsWithAutoCompleteOn)

      val result = richHtmlArgs.autoCompleteRules

      result should equal(htmlArgsWithAutoCompleteOff)
    }
  }

  private val htmlArgsMinimal: Map[Symbol, Any] = Map('title -> "test")
  private val htmlArgsWithMaxLength: Map[Symbol, Any] = Map('title -> "test", 'maxLength -> 60)
}