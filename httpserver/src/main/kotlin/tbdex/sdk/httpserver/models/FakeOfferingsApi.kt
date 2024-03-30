package tbdex.sdk.httpserver.models

import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.OfferingData
import tbdex.sdk.protocol.models.PayinDetails
import tbdex.sdk.protocol.models.PayinMethod
import tbdex.sdk.protocol.models.PayoutDetails
import tbdex.sdk.protocol.models.PayoutMethod
import web5.sdk.credentials.model.InputDescriptorV2
import web5.sdk.credentials.model.PresentationDefinitionV2

/**
 * A fake implementation of the [OfferingsApi] interface for testing purposes.
 * This class provides mock implementations for retrieving offering information.
 */
class FakeOfferingsApi : OfferingsApi {

  // Sample offering data for testing purposes
  private val offering = Offering.create(
    from = "did:ex:pfi",
    data = OfferingData(
      description = "",
      payoutUnitsPerPayinUnit = "0.000038",
      payin = PayinDetails(
        currencyCode = "USD",
        min = "0.00",
        max = "100.00",
        methods = listOf(PayinMethod(kind = "DEBIT_CARD"))
      ),
      payout = PayoutDetails(
        currencyCode = "BTC",
        methods = listOf(PayoutMethod(kind = "BTC_ADDRESS", estimatedSettlementTime = 3600))
      ),
      requiredClaims = buildPresentationDefinition()
    )
  )

  private fun buildPresentationDefinition(
    id: String = "test-pd-id",
    name: String = "simple PD",
    purpose: String = "pd for testing",
    inputDescriptors: List<InputDescriptorV2> = emptyList()
  ): PresentationDefinitionV2 {
    return PresentationDefinitionV2(
      id = id,
      name = name,
      purpose = purpose,
      inputDescriptors = inputDescriptors
    )
  }

  /**
   * Retrieves the offering with the specified ID.
   *
   * @param id The ID of the offering to retrieve. Ignored in this implementation.
   * @return The [Offering] instance for testing purposes.
   */
  override fun getOffering(id: String): Offering = offering

  /**
   * Retrieves a list of offerings based on the provided filter.
   *
   * @return A list containing the single [Offering] instance for testing purposes.
   */
  override fun getOfferings(): List<Offering> = listOf(offering)
}
