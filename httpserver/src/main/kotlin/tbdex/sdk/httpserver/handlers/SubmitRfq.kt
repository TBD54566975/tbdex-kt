package tbdex.sdk.httpserver.handlers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.httpclient.models.ErrorResponse
import tbdex.sdk.httpserver.models.CallbackError
import tbdex.sdk.httpserver.models.ExchangesApi
import tbdex.sdk.httpserver.models.OfferingsApi
import tbdex.sdk.httpserver.models.SubmitCallback
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.Rfq


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
    val errorResponse = ErrorResponse(listOf(errorDetail))
    call.respond(HttpStatusCode.Conflict, errorResponse)
    return
  }

  val offering = offeringsApi.getOffering(message.data.offeringId.toString())
  if (offering == null) {
    val errorDetail = ErrorDetail(detail = "Offering with id ${message.data.offeringId} does not exist.")
    val errorResponse = ErrorResponse(listOf(errorDetail))
    call.respond(HttpStatusCode.BadRequest, errorResponse)
    return
  }

  try {
    message.verifyOfferingRequirements(offering)
  } catch (e: Exception) {
    val errorDetail = ErrorDetail(detail = "Failed to verify offering requirements: ${e.message}")
    val errorResponse = ErrorResponse(listOf(errorDetail))
    call.respond(HttpStatusCode.BadRequest, errorResponse)
    return
  }

  if (callback == null) {
    call.respond(HttpStatusCode.Accepted)
    return
  }

  try {
    callback.invoke(call, MessageKind.rfq, offering)
  } catch (e: Exception) {
    if (e is CallbackError) {
      call.respond(e.statusCode, e.details!!.first())
      return
    } else {
      val errorDetail = ErrorDetail(detail = "umm idk")
      val errorResponse = ErrorResponse(listOf(errorDetail))
      call.respond(HttpStatusCode.InternalServerError, errorResponse)
      return
    }
  }

  call.respond(HttpStatusCode.Accepted)
}