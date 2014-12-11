package helpers.common

import composition.TestGlobal
import play.api.test.FakeApplication

object ProgressBar {
  val fakeApplicationWithProgressBarFalse = FakeApplication(
    withGlobal = Some(TestGlobal),
    additionalConfiguration = Map("progressBar.enabled" -> "false"))

  val fakeApplicationWithProgressBarTrue = FakeApplication(
    withGlobal = Some(TestGlobal),
    additionalConfiguration = Map("progressBar.enabled" -> "true"))

  def progressStep(currentStep: Int): String = {
    val end = 6
    s"Step $currentStep of $end"
  }

  final val div: String = """<div class="progress-indicator">"""
}