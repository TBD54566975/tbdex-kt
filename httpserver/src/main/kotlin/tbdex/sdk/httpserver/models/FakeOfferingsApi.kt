package tbdex.sdk.httpserver.models

import tbdex.sdk.protocol.models.CurrencyDetails
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.OfferingData
import tbdex.sdk.protocol.models.PaymentMethod

/**
 * A fake implementation of the [OfferingsApi] interface for testing purposes.
 * This class provides mock implementations for retrieving offering information.
 */
class FakeOfferingsApi : OfferingsApi {

  // Sample offering data for testing purposes
  private val offering = Offering.create(
    from = "did:ex:pfi", data = OfferingData(
    description = "",
    payinCurrency = CurrencyDetails(currencyCode = "USD"),
    payoutCurrency = CurrencyDetails(currencyCode = "BTC"),
    payoutUnitsPerPayinUnit = "0.000038",
    payinMethods = listOf(PaymentMethod(kind = "DEBIT_CARD")),
    payoutMethods = listOf(PaymentMethod(kind = "BTC_ADDRESS")),
    requiredClaims = null
  )
  )

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
   * @param filter The filter criteria for retrieving offerings. Ignored in this implementation.
   * @return A list containing the single [Offering] instance for testing purposes.
   */
  override fun getOfferings(filter: GetOfferingsFilter?): List<Offering> = listOf(offering)
}
