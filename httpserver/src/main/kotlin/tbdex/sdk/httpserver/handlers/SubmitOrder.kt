package tbdex.sdk.httpserver.handlers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.httpserver.models.CallbackError
import tbdex.sdk.httpserver.models.ErrorResponse
import tbdex.sdk.httpserver.models.ExchangesApi
import tbdex.sdk.httpserver.models.SubmitOrderCallback
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.Quote

/**
 * Handles the submission of an order by parsing the incoming message, validating the submission,
 * and executing the specified callback if provided.
 *
 * @param call The [ApplicationCall] instance representing the HTTP call.
 * @param exchangesApi The [ExchangesApi] instance for interacting with TBDex exchanges.
 * @param callback The optional callback function to be executed after successful order submission.
 */
@Suppress("TooGenericExceptionCaught", "MaxLineLength", "SwallowedException")
suspend fun submitOrder(
  call: ApplicationCall,
  exchangesApi: ExchangesApi,
  callback: SubmitOrderCallback?
) {
  val message: Order

  try {
    message = Message.parse(call.receiveText()) as Order
  } catch (e: Exception) {
    val errorDetail = ErrorDetail(detail = "Parsing of TBDex message failed: ${e.message}")
    val errorResponse = ErrorResponse(listOf(errorDetail))
    call.respond(HttpStatusCode.BadRequest, errorResponse)
    return
  }
  val exchangeId = message.metadata.exchangeId.toString()

  val exchange: List<Message>
  try {
    exchange = exchangesApi.getExchange(exchangeId)
  } catch (e: Exception) {
    val errorDetail = ErrorDetail(detail = "Could not find exchange: $exchangeId")
    call.respond(HttpStatusCode.NotFound, ErrorResponse(listOf(errorDetail)))
    return
  }

  if (!exchange.last().validNext.contains(MessageKind.order)) {
    val errorDetail =
      ErrorDetail(detail = "cannot submit Order for an exchange where the last message is kind: ${exchange.last().metadata.kind}")
    call.respond(HttpStatusCode.Conflict, ErrorResponse(listOf(errorDetail)))
    return
  }

  val quote = exchange.find { it.metadata.kind == MessageKind.quote } as? Quote
  if (quote == null) {
    val errorDetail = ErrorDetail(detail = "quote is undefined")
    call.respond(HttpStatusCode.NotFound, ErrorResponse(listOf(errorDetail)))
    return
  }

  if (message.metadata.createdAt.isAfter(quote.data.expiresAt)) {
    val errorDetail = ErrorDetail(detail = "quote is expired")
    call.respond(HttpStatusCode.Forbidden, ErrorResponse(listOf(errorDetail)))
    return
  }

  if (callback == null) {
    call.respond(HttpStatusCode.Accepted)
    return
  }

  try {
    callback.invoke(call, message)
  } catch (e: CallbackError) {
    call.respond(e.statusCode, ErrorResponse(e.details))
    return
  } catch (e: Exception) {
    val errorDetail = ErrorDetail(detail = e.message ?: "unknown error")
    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(listOf(errorDetail)))
    return
  }

  call.respond(HttpStatusCode.Accepted)
}