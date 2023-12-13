package tbdex.sdk.httpserver.models

import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.OrderStatus
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.Rfq

interface ExchangesApi {
  fun getExchange(id: List<String>? = null): List<MessageKind>?
  fun getExchanges(filter: GetExchangesFilter? = null): List<List<MessageKind>>?
  fun getRfq(exchangeId: String? = null): Rfq?
  fun getQuote(exchangeId: String? = null): Quote?
  fun getOrder(exchangeId: String? = null): Order?
  fun getOrderStatuses(exchangeId: String? = null): List<OrderStatus>?
  fun getClose(exchangeId: String? = null): Close?
}


