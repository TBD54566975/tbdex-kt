package tbdex.sdk.httpserver.models

import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.models.*

/**
 * A fake implementation of the [ExchangesApi] interface for testing purposes.
 * This class provides mock implementations for various methods.
 */
@Suppress("StringLiteralDuplication")
class FakeExchangesApi : ExchangesApi {

  /**
   * Returns a list of [MessageKind] representing exchanges with the specified ID(s).
   *
   * @param id A list of exchange IDs to retrieve. If null, returns null.
   * @return A list of [MessageKind] representing exchanges with the specified ID(s), or null if not found.
   */
  override fun getExchange(id: List<String>?): List<MessageKind>? {
    TODO("Not yet implemented")
  }

  /**
   * Returns a list of lists of [MessageKind] representing exchanges based on the provided filter.
   *
   * @param filter The filter criteria for retrieving exchanges. If null, returns all exchanges.
   * @return A list of lists of [MessageKind] representing exchanges based on the filter, or null if none are found.
   */
  override fun getExchanges(filter: GetExchangesFilter?): List<List<MessageKind>>? {
    TODO("Not yet implemented")
  }

  /**
   * Returns an [Rfq] associated with the specified exchange ID.
   *
   * @param exchangeId The ID of the exchange associated with the RFQ. If null, returns null.
   * @return An [Rfq] associated with the specified exchange, or null if not found.
   */
  override fun getRfq(exchangeId: String?): Rfq? {
    // todo: return null or an RFQ based on exchangeId somehow
    return Rfq.create(
      to = "did:ion:foo",
      from = "did:dht:bar",
      rfqData = RfqData(
        offeringId = TypeId.generate("offering"),
        payinSubunits = "100",
        payinMethod = SelectedPaymentMethod(
          kind = "USD"
        ),
        payoutMethod = SelectedPaymentMethod(
          kind = "BTC"
        ),
        claims = listOf("foo")
      )
    )
  }

  /**
   * Returns a [Quote] associated with the specified exchange ID.
   *
   * @param exchangeId The ID of the exchange associated with the Quote. If null, returns null.
   * @return A [Quote] associated with the specified exchange, or null if not found.
   */
  override fun getQuote(exchangeId: String?): Quote? {
    TODO("Not yet implemented")
  }

  /**
   * Returns an [Order] associated with the specified exchange ID.
   *
   * @param exchangeId The ID of the exchange associated with the Order. If null, returns null.
   * @return An [Order] associated with the specified exchange, or null if not found.
   */
  override fun getOrder(exchangeId: String?): Order? {
    TODO("Not yet implemented")
  }

  /**
   * Returns a list of [OrderStatus] associated with the specified exchange ID.
   *
   * @param exchangeId The ID of the exchange associated with the OrderStatus entities. If null, returns null.
   * @return A list of [OrderStatus] associated with the specified exchange, or null if none are found.
   */
  override fun getOrderStatuses(exchangeId: String?): List<OrderStatus>? {
    TODO("Not yet implemented")
  }

  /**
   * Returns a [Close] associated with the specified exchange ID.
   *
   * @param exchangeId The ID of the exchange associated with the Close entity. If null, returns null.
   * @return A [Close] associated with the specified exchange, or null if not found.
   */
  override fun getClose(exchangeId: String?): Close? {
    TODO("Not yet implemented")
  }
}
