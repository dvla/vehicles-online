package composition

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WithDefaultApplication

trait WithApplication extends WithDefaultApplication with DisposeGlobalCreator
