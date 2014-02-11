package helpers.disposal_of_vehicle

import play.api.test.TestBrowser
import org.scalatest.{Matchers, WordSpec}
import org.specs2.mock.Mockito
import helpers.disposal_of_vehicle.DOVValidValues._
import mappings.disposal_of_vehicle.BusinessAddressSelect._

object BusinessChooseYourAddress extends WordSpec with Matchers with Mockito {
  def businessChooseYourAddressPopulate(browser: TestBrowser, businessName: String = traderBusinessNameValid) = {
    browser.goTo("/disposal-of-vehicle/business-choose-your-address")
    browser.fill(s"#${businessNameId}") `with` businessName
    browser.click(s"#${addressSelectId} option[value='${firstAddress}']")
    browser.submit("button[type='submit']")
  }
}
