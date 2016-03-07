package gov.uk.dvla.vehicles.dispose.helpers

import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import org.scalatest.Matchers
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WithClue
import uk.gov.dvla.vehicles.presentation.common.testhelpers.ScaleFactor

trait AcceptanceTestHelper
  extends ScalaDsl
  with EN
  with Matchers
  with WithClue
  with ScaleFactor
  with Eventually
  with IntegrationPatience
