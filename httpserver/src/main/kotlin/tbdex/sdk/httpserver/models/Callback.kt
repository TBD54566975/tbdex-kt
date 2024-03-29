package tbdex.sdk.httpserver.models

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Offering

/**
 * Represents a callback function for handling GET requests with specified filters.
 *
 * @param ApplicationCall The Ktor application call object representing the incoming HTTP request.
 * @param Filter The filter used in the GET request.
 * @return Any result returned from the callback.
 */
typealias GetCallback = suspend (ApplicationCall, Filter?) -> Any

/**
 * Represents a callback function for handling submit requests with received message and associated offering.
 *
 * @param ApplicationCall The Ktor application call object representing the incoming HTTP request.
 * @param Message the message received in the request to be processed further by the callback function
 * @param Offering The offering associated with the submitted message.
 * @param ReplyTo The replyTo URL if provided in the CreateExchange request
 */
typealias SubmitCallback = suspend (ApplicationCall, Message, Offering?, String?) -> Unit

/**
 * Enum representing the kinds of messages that can be submitted.
 */
enum class SubmitKind {
  rfq, order, close
}

/**
 * Enum representing the kinds of resources that can be retrieved.
 */
enum class GetKind {
  exchanges, offerings
}

/**
 * Represents a filter for retrieving offerings based on specific criteria.
 *
 * @property payinCurrency The currency used for the pay-in.
 * @property payoutCurrency The currency used for the payout.
 * @property id The ID of the offering.
 */
class GetOfferingsFilter(
  val payinCurrency: String? = null,
  val payoutCurrency: String? = null,
  val id: String? = null
) : Filter

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
