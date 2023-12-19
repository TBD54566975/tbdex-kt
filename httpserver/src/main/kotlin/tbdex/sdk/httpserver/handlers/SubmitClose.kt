package tbdex.sdk.httpserver.handlers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.httpserver.models.CallbackError
import tbdex.sdk.httpserver.models.ErrorResponse
import tbdex.sdk.httpserver.models.ExchangesApi
import tbdex.sdk.httpserver.models.SubmitCallback
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.MessageKind

@Suppress("TooGenericExceptionCaught")
suspend fun submitClose(
  call: ApplicationCall,
  exchangesApi: ExchangesApi
  callback: SubmitCallback?
) {
  val message: Close?

  try {
    message = Message.parse(call.receiveText()) as Close
  } catch (e: Exception) {
    val errorDetail = ErrorDetail(detail = "Parsing of TBDex message failed: ${e.message}")
    val errorResponse = ErrorResponse(listOf(errorDetail))
    call.respond(HttpStatusCode.BadRequest, errorResponse)
    return
  }

  val existingRfq = exchangesApi.getRfq(message.metadata.exchangeId.toString())
  if (existingRfq != null) {
    val errorDetail = ErrorDetail(detail = "RFQ already exists.")
    call.respond(HttpStatusCode.Conflict, ErrorResponse(listOf(errorDetail)))
    return
  }

  if (callback == null) {
    call.respond(HttpStatusCode.Accepted)
    return
  }

  try {
    callback.invoke(call, MessageKind.close, null)
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