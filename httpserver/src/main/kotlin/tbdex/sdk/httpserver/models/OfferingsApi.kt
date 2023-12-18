package tbdex.sdk.httpserver.models

import tbdex.sdk.protocol.models.Offering

/**
 * Interface representing an API for interacting with TBDex offerings.
 */
interface OfferingsApi {

  /**
   * Retrieves the offering with the specified ID.
   *
   * @param id The ID of the offering to retrieve. If null, returns null.
   * @return The [Offering] with the specified ID, or null if not found.
   */
  fun getOffering(id: String? = null): Offering?

  /**
   * Retrieves a list of offerings based on the provided filter.
   *
   * @param filter The filter criteria for retrieving offerings. If null, returns all offerings.
   * @return A list of [Offering] objects matching the filter, or null if none are found.
   */
  fun getOfferings(filter: GetOfferingsFilter? = null): List<Offering>?
}

