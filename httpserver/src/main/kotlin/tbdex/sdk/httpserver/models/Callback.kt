package tbdex.sdk.httpserver.models

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.Rfq

typealias GetOfferingsCallback = suspend (ApplicationCall) -> Any
typealias GetExchangesCallback = suspend (ApplicationCall, GetExchangesFilter?) -> Any
typealias GetExchangeCallback = suspend (ApplicationCall) -> Any

typealias CreateExchangeCallback = suspend (ApplicationCall, Rfq, Offering, String?) -> Unit
typealias SubmitOrderCallback = suspend (ApplicationCall, Order) -> Unit
typealias SubmitCloseCallback = suspend (ApplicationCall, Close) -> Unit


/**
 * Represents a filter for retrieving exchanges based on specific criteria.
 *
 * @property exchangeIds The list of exchange IDs to filter by.
 * @property from The source identifier for exchanges.
 */
class GetExchangesFilter(
  val exchangeIds: List<String>? = null,
  // todo: make non-nullable once we can pass in requesterDid
  // from verifying JWT. see issue: https://github.com/TBD54566975/tbdex/issues/210
  val from: String? = null
) : Filter

/**
 * Interface representing a generic filter for API requests.
 */
sealed interface Filter

/**
 * Exception class representing an error that can occur during a callback execution.
 *
 * @property statusCode The HTTP status code associated with the error.
 * @property details The list of error details providing additional information.
 */
class CallbackError(val statusCode: HttpStatusCode, val details: List<ErrorDetail>) : Exception()

/**
 * Error response
 *
 * @property errors list of errors
 * @constructor
 */
class ErrorResponse(val errors: List<ErrorDetail>)
