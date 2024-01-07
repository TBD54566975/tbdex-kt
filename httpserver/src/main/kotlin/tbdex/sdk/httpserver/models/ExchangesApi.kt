package tbdex.sdk.httpserver.models

import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.OrderStatus
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.Rfq

/**
 * Interface representing an API for interacting with TBDex exchanges and related entities.
 */
interface ExchangesApi {

  /**
   * Retrieves the exchange with the specified ID.
   *
   * @param id The ID of the exchange to retrieve.
   * @return A list of [Message] representing the exchange with the specified ID.
   */
  fun getExchange(id: String): List<Message>

  /**
   * Retrieves a list of exchanges based on the provided filter.
   *
   * @param filter The filter criteria for retrieving exchanges. If null, returns all exchanges.
   * @return A list of lists of [Message] representing exchanges based on the filter.
   */
  fun getExchanges(filter: GetExchangesFilter? = null): List<List<Message>>

  /**
   * Retrieves the Request for Quote (RFQ) for the specified exchange ID.
   *
   * @param exchangeId The ID of the exchange to retrieve the RFQ for.
   * @return The [Rfq] object representing the RFQ for the specified exchange.
   */
  fun getRfq(exchangeId: String): Rfq

  /**
   * Retrieves the Quote for the specified exchange ID.
   *
   * @param exchangeId The ID of the exchange to retrieve the Quote for.
   * @return The [Quote] object representing the Quote for the specified exchange.
   */
  fun getQuote(exchangeId: String): Quote

  /**
   * Retrieves the Order for the specified exchange ID.
   *
   * @param exchangeId The ID of the exchange to retrieve the Order for.
   * @return The [Order] object representing the Order for the specified exchange.
   */
  fun getOrder(exchangeId: String): Order

  /**
   * Retrieves the list of Order Statuses for the specified exchange ID.
   *
   * @param exchangeId The ID of the exchange to retrieve Order Statuses for.
   * @return A list of [OrderStatus] objects representing the Order Statuses for the specified exchange, or an empty list if none are found.
   */
  fun getOrderStatuses(exchangeId: String): List<OrderStatus>

  /**
   * Retrieves the Close information for the specified exchange ID.
   *
   * @param exchangeId The ID of the exchange to retrieve the Close information for.
   * @return The [Close] object representing the Close information for the specified exchange.
   */
  fun getClose(exchangeId: String): Close
}
