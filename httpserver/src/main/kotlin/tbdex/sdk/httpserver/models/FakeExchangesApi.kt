package tbdex.sdk.httpserver.models

import tbdex.sdk.protocol.models.*

/**
 * A fake implementation of the [ExchangesApi] interface for testing purposes.
 * This class provides mock implementations for various methods.
 */
@Suppress("StringLiteralDuplication")
class FakeExchangesApi : ExchangesApi {
  var exchanges: MutableMap<String, List<Message>> = mutableMapOf()

  /**
   * Returns a list of [Message] representing the exchange with the specified ID.
   *
   * @param id The exchange ID to retrieve. If null, returns null.
   * @return A list of [Message] representing exchanges with the specified ID(s), or null if not found.
   */
  override fun getExchange(id: String): List<Message>? {
    return exchanges[id]
  }

  /**
   * Returns a list of lists of [Message] representing exchanges based on the provided filter.
   *
   * @param filter The filter criteria for retrieving exchanges. If null, returns all exchanges.
   * @return A list of lists of [Message] representing exchanges based on the filter, or null if none are found.
   */
  override fun getExchanges(filter: GetExchangesFilter?): List<List<Message>>? {
    TODO("Not yet implemented")
  }

  /**
   * Returns an [Rfq] associated with the specified exchange ID.
   *
   * @param exchangeId The ID of the exchange associated with the RFQ.
   * @return An [Rfq] associated with the specified exchange, or null if not found.
   */
  override fun getRfq(exchangeId: String): Rfq? {
    return exchanges[exchangeId]?.find { it.metadata.kind == MessageKind.rfq } as? Rfq
  }

  /**
   * Returns a [Quote] associated with the specified exchange ID.
   *
   * @param exchangeId The ID of the exchange associated with the Quote.
   * @return A [Quote] associated with the specified exchange, or null if not found.
   */
  override fun getQuote(exchangeId: String): Quote? {
    TODO("Not yet implemented")
  }

  /**
   * Returns an [Order] associated with the specified exchange ID.
   *
   * @param exchangeId The ID of the exchange associated with the Order.
   * @return An [Order] associated with the specified exchange, or null if not found.
   */
  override fun getOrder(exchangeId: String): Order? {
    TODO("Not yet implemented")
  }

  /**
   * Returns a list of [OrderStatus] associated with the specified exchange ID.
   *
   * @param exchangeId The ID of the exchange associated with the OrderStatus entities.
   * @return A list of [OrderStatus] associated with the specified exchange, or null if none are found.
   */
  override fun getOrderStatuses(exchangeId: String): List<OrderStatus>? {
    TODO("Not yet implemented")
  }

  /**
   * Returns a [Close] associated with the specified exchange ID.
   *
   * @param exchangeId The ID of the exchange associated with the Close entity.
   * @return A [Close] associated with the specified exchange, or null if not found.
   */
  override fun getClose(exchangeId: String): Close? {
    TODO("Not yet implemented")
  }

  fun addMessage(message: Message) {
    val exchangeId = message.metadata.exchangeId.toString()
    val messages = this.exchanges[exchangeId]?.plus(message) ?: listOf(message)
    this.exchanges[exchangeId] = messages
  }

  fun resetExchanges() {
    this.exchanges = mutableMapOf()
  }
}
