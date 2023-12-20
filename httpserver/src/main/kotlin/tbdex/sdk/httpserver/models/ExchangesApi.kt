package tbdex.sdk.httpserver.models

import tbdex.sdk.protocol.models.*

/**
 * Interface representing an API for interacting with TBDex exchanges and related entities.
 */
interface ExchangesApi {

  /**
   * Retrieves the exchange with the specified ID.
   *
   * @param id The exchange IDs to retrieve.
   * @return A list of [Message] representing exchange with the specified ID, or null if not found.
   */
  fun getExchange(id: String): List<Message>?

  /**
   * Retrieves a list of exchanges based on the provided filter.
   *
   * @param filter The filter criteria for retrieving exchanges. If null, returns all exchanges.
   * @return A list of lists of [Message] representing exchanges based on the filter, or null if none are found.
   */
  fun getExchanges(filter: GetExchangesFilter? = null): List<List<Message>>?

  fun getRfq(exchangeId: String): Rfq?

  fun getQuote(exchangeId: String): Quote?

  fun getOrder(exchangeId: String): Order?

  fun getOrderStatuses(exchangeId: String): List<OrderStatus>?

  fun getClose(exchangeId: String): Close?
}
