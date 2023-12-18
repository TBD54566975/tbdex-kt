package tbdex.sdk.httpserver.models

import tbdex.sdk.protocol.models.CurrencyDetails
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.OfferingData
import tbdex.sdk.protocol.models.PaymentMethod
import web5.sdk.credentials.ConstraintsV2
import web5.sdk.credentials.InputDescriptorV2
import web5.sdk.credentials.PresentationDefinitionV2

class FakeOfferingsApi : OfferingsApi {
  private val offering = Offering.create(
    from = "did:ex:pfi", data = OfferingData(
      description = "",
      payinCurrency = CurrencyDetails(currencyCode = "USD"),
      payoutCurrency = CurrencyDetails(currencyCode = "BTC"),
      payoutUnitsPerPayinUnit = "0.000038",
      payinMethods = listOf(PaymentMethod(kind = "DEBIT_CARD")),
      payoutMethods = listOf(PaymentMethod(kind = "BTC_ADDRESS")),
      requiredClaims = PresentationDefinitionV2(
        id = "7ce4004c-3c38-4853-968b-e411bafcd945", inputDescriptors = listOf(
          InputDescriptorV2(id = "test-input-descriptor", constraints = ConstraintsV2())
        )
      )
    )
  )

  override fun getOffering(id: String?): Offering = offering

  override fun getOfferings(filter: GetOfferingsFilter?): List<Offering> = listOf(offering)
}