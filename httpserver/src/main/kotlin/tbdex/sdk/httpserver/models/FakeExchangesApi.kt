package tbdex.sdk.httpserver.models

import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.OrderStatus
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.models.RfqData
import tbdex.sdk.protocol.models.SelectedPaymentMethod

class FakeExchangesApi : ExchangesApi {
  override fun getExchange(id: List<String>?): List<MessageKind>? {
    TODO("Not yet implemented")
  }

  override fun getExchanges(filter: GetExchangesFilter?): List<List<MessageKind>>? {
    TODO("Not yet implemented")
  }

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

  override fun getQuote(exchangeId: String?): Quote? {
    TODO("Not yet implemented")
  }

  override fun getOrder(exchangeId: String?): Order? {
    TODO("Not yet implemented")
  }

  override fun getOrderStatuses(exchangeId: String?): List<OrderStatus>? {
    TODO("Not yet implemented")
  }

  override fun getClose(exchangeId: String?): Close? {
    TODO("Not yet implemented")
  }
}