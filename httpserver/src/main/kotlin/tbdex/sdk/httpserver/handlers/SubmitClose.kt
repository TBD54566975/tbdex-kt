package tbdex.sdk.httpserver.handlers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.httpserver.models.CallbackError
import tbdex.sdk.httpserver.models.ErrorResponse
import tbdex.sdk.httpserver.models.ExchangesApi
import tbdex.sdk.httpserver.models.SubmitCloseCallback
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.MessageKind

/**
 * Handles the submission of a close message by parsing the incoming message,
 * validating the submission, and executing the specified callback if provided.
 *
 * @param call The [ApplicationCall] instance representing the HTTP call.
 * @param exchangesApi The [ExchangesApi] instance for interacting with TBDex exchanges.
 * @param callback The optional callback function to be executed after a successful close submission.
 */
@Suppress("TooGenericExceptionCaught", "MaxLineLength", "SwallowedException")
suspend fun submitClose(
  call: ApplicationCall,
  exchangesApi: ExchangesApi,
  callback: SubmitCloseCallback?,
  close: Close,
) {

  val exchangeId = close.metadata.exchangeId
  val exchange: List<Message>
  try {
    exchange = exchangesApi.getExchange(exchangeId)
  } catch (e: NoSuchElementException) {
    val errorDetail = ErrorDetail(detail = "Could not find exchange: $exchangeId")
    call.respond(HttpStatusCode.NotFound, ErrorResponse(listOf(errorDetail)))
    return
  } catch (e: Exception) {
    val errorDetail = ErrorDetail(detail = "Failed to fetch exchange: $exchangeId")
    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(listOf(errorDetail)))
    return
  }

  if (close.metadata.protocol != exchange.first().metadata.protocol) {
    val errorDetail =
      ErrorDetail(detail = "Protocol mismatch: ${close.metadata.protocol} != ${exchange.first().metadata.protocol}")
    call.respond(HttpStatusCode.Conflict, ErrorResponse(listOf(errorDetail)))
    return
  }

  if (!exchange.last().validNext.contains(MessageKind.close)) {
    val errorDetail =
      ErrorDetail(detail = "cannot submit Order for an exchange where the last message is kind: ${exchange.last().metadata.kind}")
    call.respond(HttpStatusCode.Conflict, ErrorResponse(listOf(errorDetail)))
    return
  }

  if (callback == null) {
    call.respond(HttpStatusCode.Accepted)
    return
  }

  try {
    callback.invoke(call, close)
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