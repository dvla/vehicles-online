package views.disposal_of_vehicle

import composition.TestHarness
import helpers.UiSpec
import helpers.common.ProgressBar
import helpers.tags.UiTag
import pages.disposal_of_vehicle.OnHandlerNotFoundPage.exit
import pages.disposal_of_vehicle.{BeforeYouStartPage, OnHandlerNotFoundPage}

final class OnHandlerNotFoundIntegrationSpec extends UiSpec with TestHarness {
  "go to not found page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to OnHandlerNotFoundPage
      page.title should equal(OnHandlerNotFoundPage.title)
    }

    "not display any progress indicator when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to OnHandlerNotFoundPage
      page.source should not contain ProgressBar.div
    }
  }

  "exit" should {
    "redirect to BeforeYouStartPage" taggedAs UiTag in new WebBrowser {
      go to OnHandlerNotFoundPage
      click on exit
      page.title should equal(BeforeYouStartPage.title)
    }
  }
}