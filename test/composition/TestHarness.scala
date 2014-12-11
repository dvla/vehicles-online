package composition

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.TestHarnessBase

trait TestHarness extends TestHarnessBase with DisposeGlobalCreator
