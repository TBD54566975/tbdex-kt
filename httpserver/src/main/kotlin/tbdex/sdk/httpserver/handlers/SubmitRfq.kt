package tbdex.sdk.httpserver.handlers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.httpserver.models.*
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.Rfq

/**
 * Handles the submission of a Request for Quote (RFQ) through the TBDex API.
 *
 * This function parses an RFQ message, performs necessary validations, and invokes the callback
 * if provided. It responds with appropriate HTTP status codes for success or failure scenarios.
 *
 * @param call The Ktor application call object representing the incoming HTTP request.
 * @param offeringsApi An instance of [OfferingsApi] for interacting with offerings data.
 * @param exchangesApi An instance of [ExchangesApi] for interacting with exchanges data.
 * @param callback An optional callback function to be invoked after processing the RFQ.
 */
@Suppress("TooGenericExceptionCaught")
suspend fun submitRfq(
  call: ApplicationCall,
  offeringsApi: OfferingsApi,
  exchangesApi: ExchangesApi,
  callback: SubmitCallback?
) {
  val message: Rfq?

  try {
    message = Message.parse(call.receiveText()) as Rfq
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

  val offering = offeringsApi.getOffering(message.data.offeringId.toString())
  if (offering == null) {
    val errorDetail = ErrorDetail(detail = "Offering with id ${message.data.offeringId} does not exist.")
    call.respond(HttpStatusCode.BadRequest, ErrorResponse(listOf(errorDetail)))
    return
  }

  try {
    message.verifyOfferingRequirements(offering)
  } catch (e: Exception) {
    val errorDetail = ErrorDetail(detail = "Failed to verify offering requirements: ${e.message}")
    call.respond(HttpStatusCode.BadRequest, ErrorResponse(listOf(errorDetail)))
    return
  }

  if (callback == null) {
    call.respond(HttpStatusCode.Accepted)
    return
  }

  try {
    callback.invoke(call, MessageKind.rfq, offering)
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