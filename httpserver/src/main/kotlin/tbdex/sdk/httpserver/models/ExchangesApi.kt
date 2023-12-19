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

  /**
   * Retrieves the Request for Quote (RFQ) associated with the specified exchange.
   *
   * @param exchangeId The ID of the exchange associated with the RFQ. If null, returns null.
   * @return The [Rfq] associated with the specified exchange, or null if not found.
   */
  fun getRfq(exchangeId: String? = null): Rfq?

  /**
   * Retrieves the Quote associated with the specified exchange.
   *
   * @param exchangeId The ID of the exchange associated with the Quote. If null, returns null.
   * @return The [Quote] associated with the specified exchange, or null if not found.
   */
  fun getQuote(exchangeId: String? = null): Quote?

  /**
   * Retrieves the Order associated with the specified exchange.
   *
   * @param exchangeId The ID of the exchange associated with the Order. If null, returns null.
   * @return The [Order] associated with the specified exchange, or null if not found.
   */
  fun getOrder(exchangeId: String? = null): Order?

  /**
   * Retrieves the list of OrderStatus entities associated with the specified exchange.
   *
   * @param exchangeId The ID of the exchange associated with the OrderStatus entities. If null, returns null.
   * @return A list of [OrderStatus] associated with the specified exchange, or null if none are found.
   */
  fun getOrderStatuses(exchangeId: String? = null): List<OrderStatus>?

  /**
   * Retrieves the Close entity associated with the specified exchange.
   *
   * @param exchangeId The ID of the exchange associated with the Close entity. If null, returns null.
   * @return The [Close] associated with the specified exchange, or null if not found.
   */
  fun getClose(exchangeId: String? = null): Close?
}
