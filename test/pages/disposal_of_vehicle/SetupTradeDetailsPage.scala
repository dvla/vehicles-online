package pages.disposal_of_vehicle

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import uk.gov.dvla.vehicles.presentation.common.mappings.Email._
import uk.gov.dvla.vehicles.presentation.common.mappings.OptionalToggle._
import uk.gov.dvla.vehicles.presentation.common.model.SetupTradeDetailsFormModel.Form._
import views.disposal_of_vehicle.SetupTradeDetails.SubmitId
import webserviceclients.fakes.FakeAddressLookupService.{PostcodeValid, PostcodeWithoutAddresses, TraderBusinessNameValid}

trait SetupTradeDetailsPageBase extends Page {
  def address: String

  final val title: String = "Provide trader details"

  lazy val url: String = WebDriverFactory.testUrl + address.substring(1)
  lazy val cegUrl: String = WebDriverFactory.testUrl + address.substring(1) + "/ceg"

  def traderName(implicit driver: WebDriver): TextField = textField(id(TraderNameId))

  def traderPostcode(implicit driver: WebDriver): TextField = textField(id(TraderPostcodeId))

  def lookup(implicit driver: WebDriver): Element = find(id(SubmitId)).get
}

object SetupTradeDetailsPage extends SetupTradeDetailsPageBase {
  final val address = s"$applicationContext/setup-trade-details"

  final val TraderEmailValid = "example@example.co.uk"

  def traderEmail(implicit driver: WebDriver): EmailField = emailField(id(s"${TraderEmailId}_$EmailId"))

  def traderConfirmEmail(implicit driver: WebDriver): EmailField = emailField(id(s"${TraderEmailId}_$EmailVerifyId"))

  def emailVisible(implicit driver: WebDriver): RadioButton =
    radioButton(id(s"${TraderEmailOptionId}_$Visible"))

  def emailInvisible(implicit driver: WebDriver): RadioButton =
    radioButton(id(s"${TraderEmailOptionId}_$Invisible"))

  def happyPath(traderBusinessName: String = TraderBusinessNameValid,
                traderBusinessPostcode: String = PostcodeValid,
                traderBusinessEmail: Option[String] = Some(TraderEmailValid))
               (implicit driver: WebDriver) = {
    go to SetupTradeDetailsPage
    traderName.value = traderBusinessName
    traderPostcode.value = traderBusinessPostcode
    traderBusinessEmail.fold(click on emailInvisible) { email =>
      click on emailVisible
      traderEmail.value = email
      traderConfirmEmail.value = email
    }
    click on lookup
  }

  def submitPostcodeWithoutAddresses(implicit driver: WebDriver) = {
    go to SetupTradeDetailsPage
    traderName.value = TraderBusinessNameValid
    traderPostcode.value = PostcodeWithoutAddresses
    click on emailInvisible
    click on lookup
  }
}
