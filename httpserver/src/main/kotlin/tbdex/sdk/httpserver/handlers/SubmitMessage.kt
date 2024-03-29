package tbdex.sdk.httpserver.handlers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.httpserver.models.ErrorResponse
import tbdex.sdk.httpserver.models.ExchangesApi
import tbdex.sdk.httpserver.models.SubmitCloseCallback
import tbdex.sdk.httpserver.models.SubmitOrderCallback
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.Order

/**
 * Handles the submission of a close message by parsing the incoming message,
 * validating the submission, and executing the specified callback if provided.
 *
 * @param call The [ApplicationCall] instance representing the HTTP call.
 * @param exchangesApi The [ExchangesApi] instance for interacting with TBDex exchanges.
 * @param callback The optional callback function to be executed after a successful close submission.
 */
@Suppress("TooGenericExceptionCaught", "MaxLineLength", "SwallowedException")
suspend fun submitMessage(
  call: ApplicationCall,
  exchangesApi: ExchangesApi,
  callback: Any?
) {
  val message: Message

  try {
    message = Message.parse(call.receiveText())
  } catch (e: Exception) {
    val errorDetail = ErrorDetail(detail = "Parsing of TBDex message failed: ${e.message}")
    val errorResponse = ErrorResponse(listOf(errorDetail))
    call.respond(HttpStatusCode.BadRequest, errorResponse)
    return
  }

  if (message.metadata.exchangeId != call.parameters["exchangeId"]) {
    val errorDetail = ErrorDetail(detail = "Exchange ID of message must match URL")
    val errorResponse = ErrorResponse(listOf(errorDetail))
    call.respond(HttpStatusCode.BadRequest, errorResponse)
    return
  }

  when (message.metadata.kind) {
    MessageKind.close -> {
      return submitClose(call, exchangesApi, callback as SubmitCloseCallback, message as Close)
    }
    MessageKind.order -> {
      return submitOrder(call, exchangesApi, callback as SubmitOrderCallback, message as Order)
    }
    else -> {
      val errorDetail = ErrorDetail(detail = "Message must be a valid Order or Close message")
      call.respond(HttpStatusCode.BadRequest, ErrorResponse(listOf(errorDetail)))
    }
  }
}