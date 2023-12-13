package tbdex.server

import io.ktor.server.application.ApplicationCall
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.OrderStatus
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.Rfq

//typealias GetCallback<T> = (RequestContext, Filters[T]) -> Any
//typealias SubmitCallback<T, O> = (RequestContext, MessageKindClasses[T], SubmitCallbackOpts[O]) -> Unit

typealias GetCallback = (ApplicationCall) -> Any
typealias SubmitCallback = (ApplicationCall, MessageKind, Submit) -> Unit

public interface OfferingsApi {
  fun getOffering(opts: GetOfferingOpts): Offering?
  fun getOfferings(opts: GetOfferingsOpts): List<Offering>?

}

class GetOfferingOpts {
  val id: String? = null
}

class GetOfferingsOpts {
  val filter: GetOfferingsFilter? = null
}

class GetOfferingsFilter {
  val payinCurrency: String? = null
  val payoutCurrency: String? = null
  val id: String? = null
}

public interface ExchangesApi {
  fun getExchange(opts: GetExchangeOpts): List<MessageKind>?
  fun getExchanges(opts: GetExchangesOpts): List<List<MessageKind>>?
  fun getRfq(opts: GetMessageOpts): Rfq?
  fun getQuote(opts: GetMessageOpts): Quote?
  fun getOrder(opts: GetMessageOpts): Order?
  fun getOrderStatuses(opts: GetMessageOpts): List<OrderStatus>?
  fun getClose(opts: GetMessageOpts): Close?
}

class GetExchangeOpts {
  val id: List<String>? = null
}

class GetExchangesOpts {
  val filter: GetExchangesFilter? = null

}

class GetExchangesFilter {
  val id: List<String>? = null
  val from: String? = null
}

class GetMessageOpts {
  val exchangeId: String? = null
}
