package controllers

import helpers.TestWithApplication
import helpers.UnitSpec
import models.BusinessChooseYourAddressFormModel.Form.AddressSelectId
import org.mockito.invocation.InvocationOnMock
import org.mockito.Matchers.{any, anyString}
import org.mockito.Mockito.when
import org.mockito.stubbing.Answer
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.services.DateServiceImpl
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.AddressLookupServiceImpl
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStats
import utils.helpers.Config
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddress
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddressNotFound
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.selectedAddress

class BusinessChooseYourAddressFormSpec extends UnitSpec {
  "form" should {
    "accept when all fields contain valid responses" in new TestWithApplication {
      formWithValidDefaults().get.addressSelected should equal(selectedAddress)
    }
  }

  "addressSelect" should {
    "reject if empty" in new TestWithApplication {
      val errors = formWithValidDefaults(addressSelected = "").errors
      errors.length should equal(1)
      errors.head.key should equal(AddressSelectId)
      errors.head.message should equal("error.required")
    }
  }

  private def businessChooseYourAddressWithFakeWebService(addressFound: Boolean = true) = {
    val responsePostcode = if (addressFound) responseValidForPostcodeToAddress
                           else responseValidForPostcodeToAddressNotFound
    val fakeWebService = new FakeAddressLookupWebServiceImpl(responsePostcode)
    val healthStatsMock = mock[HealthStats]
    when(healthStatsMock.report(anyString)(any[Future[_]])).thenAnswer(new Answer[Future[_]] {
      override def answer(invocation: InvocationOnMock): Future[_] =
        invocation.getArguments()(1).asInstanceOf[Future[_]]
    })
    val addressLookupService = new AddressLookupServiceImpl(fakeWebService, new DateServiceImpl, healthStatsMock)
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    new BusinessChooseYourAddress(addressLookupService)
  }

  private def formWithValidDefaults(addressSelected: String = selectedAddress) = {
    businessChooseYourAddressWithFakeWebService().form.bind(
      Map(AddressSelectId -> addressSelected)
    )
  }
}
