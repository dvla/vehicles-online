package helpers.common

import composition.TestGlobal
import play.api.test.FakeApplication
import uk.gov.dvla.vehicles.presentation.common.testhelpers.LightFakeApplication

object ProgressBar {
  val fakeApplicationWithProgressBarFalse = LightFakeApplication(TestGlobal, Map("progressBar.enabled" -> "false"))

  val fakeApplicationWithProgressBarTrue = LightFakeApplication(TestGlobal, Map("progressBar.enabled" -> "true"))

  def progressStep(currentStep: Int): String = {
    val end = 6
    s"Step $currentStep of $end"
  }

  final val div: String = """<div class="progress-indicator">"""
}