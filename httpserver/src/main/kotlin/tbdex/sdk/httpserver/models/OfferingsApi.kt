package tbdex.sdk.httpserver.models

import tbdex.sdk.protocol.models.Offering

/**
 * Interface representing an API for interacting with TBDex offerings.
 */
interface OfferingsApi {

  /**
   * Retrieves the offering with the specified ID.
   *
   * @param id The ID of the offering to retrieve.
   * @return The [Offering] with the specified ID.
   *
   * @throws NoSuchElementException if the offering with the specified ID is not found.
   * @throws Exception for general exceptions.
   */
  @Throws(NoSuchElementException::class, Exception::class)
  fun getOffering(id: String): Offering

  /**
   * Retrieves a list of offerings based on the provided filter.
   *
   * @return A list of [Offering] objects matching the filter.
   *
   * @throws Exception if any of the offerings are not found.
   */
  @Throws(Exception::class)
  fun getOfferings(): List<Offering>
}
