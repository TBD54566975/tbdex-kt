package tbdex.sdk.httpserver.models

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.Offering

typealias GetCallback = (ApplicationCall, Filter) -> Any
typealias SubmitCallback = (ApplicationCall, MessageKind, Offering?) -> Unit

enum class SubmitKind {
  rfq, order, close
}

enum class GetKind {
  exchanges, offerings
}

class GetOfferingsFilter(
  val payinCurrency: String? = null,
  val payoutCurrency: String? = null,
  val id: String? = null
) : Filter

class GetExchangesFilter(
  val exchangeIds: List<String>,
  val from: String
) : Filter

sealed interface Filter
class CallbackError(val statusCode: HttpStatusCode, val details: List<ErrorDetail>?) : Exception()

class ErrorResponse(val errors: List<ErrorDetail>?)