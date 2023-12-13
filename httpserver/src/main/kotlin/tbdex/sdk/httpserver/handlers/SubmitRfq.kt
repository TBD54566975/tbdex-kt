package tbdex.server.tbdex.sdk.httpserver.handlers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.httpserver.models.ExchangesApi
import tbdex.sdk.httpserver.models.OfferingsApi
import tbdex.sdk.httpserver.models.SubmitCallback
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.Rfq

// validate the message and then invoke submitCallback
suspend fun submitRfq(
  call: ApplicationCall,
  offeringsApi: OfferingsApi,
  exchangesApi: ExchangesApi,
  callback: SubmitCallback?
) {
  try {
    val message = Message.parse(call.receiveText()) as Rfq
    val offering = offeringsApi.getOffering(message.data.offeringId.toString())

    callback?.invoke(call, MessageKind.rfq, offering)
    call.respond(HttpStatusCode.Accepted)

  } catch (e: Exception) {
    val errorResponse = ErrorDetail(detail = "Parsing of TBDex message failed: ${e.message}")
    call.respond(HttpStatusCode.BadRequest, errorResponse)
  }
}