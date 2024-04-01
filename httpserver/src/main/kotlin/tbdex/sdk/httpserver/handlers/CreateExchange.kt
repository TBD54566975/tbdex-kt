package tbdex.sdk.httpserver.handlers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.httpserver.models.CallbackError
import tbdex.sdk.httpserver.models.CreateExchangeCallback
import tbdex.sdk.httpserver.models.ErrorResponse
import tbdex.sdk.httpserver.models.ExchangesApi
import tbdex.sdk.httpserver.models.OfferingsApi
import tbdex.sdk.protocol.Parser
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.serialization.Json
import java.net.URL

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
@Suppress("TooGenericExceptionCaught", "SwallowedException")
suspend fun createExchange(
  call: ApplicationCall,
  offeringsApi: OfferingsApi,
  exchangesApi: ExchangesApi,
  callback: CreateExchangeCallback?
) {
  val rfq: Rfq?
  var replyTo: String? = null
  try {
    val requestBody = call.receiveText()

    val jsonNode = Json.jsonMapper.readTree(requestBody)
    val rfqJsonString = jsonNode["rfq"].toString()

    rfq = Rfq.parse(rfqJsonString)
    if (jsonNode["replyTo"] != null) {
      replyTo = jsonNode["replyTo"].asText()
    }
  } catch (e: Exception) {
    val errorDetail = ErrorDetail(detail = "Parsing of TBDex createExchange request failed: ${e.message}")
    val errorResponse = ErrorResponse(listOf(errorDetail))
    call.respond(HttpStatusCode.BadRequest, errorResponse)
    return
  }

  if (replyTo != null && !isValidUrl(replyTo)) {
    val errorDetail = ErrorDetail(detail = "replyTo must be a valid URL")
    val errorResponse = ErrorResponse(listOf(errorDetail))
    call.respond(HttpStatusCode.BadRequest, errorResponse)
    return
  }

  try {
    exchangesApi.getExchange(rfq.metadata.exchangeId, rfq.metadata.from)
    val errorDetail = ErrorDetail(detail = "RFQ already exists.")
    call.respond(HttpStatusCode.Conflict, ErrorResponse(listOf(errorDetail)))
    return
  } catch (_: NoSuchElementException) {
    // exchangesApi.getExchange throws if no existing exchange is found
  }

  val offering: Offering
  try {
    offering = offeringsApi.getOffering(rfq.data.offeringId)

    rfq.verifyOfferingRequirements(offering)
  } catch (e: NoSuchElementException) {
    val errorDetail = ErrorDetail(detail = "Offering with id ${rfq.data.offeringId} does not exist.")
    call.respond(HttpStatusCode.BadRequest, ErrorResponse(listOf(errorDetail)))
    return
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
    callback.invoke(call, rfq, offering, replyTo)
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

/**
 * Checks if a string is a valid URL.
 *
 * @param replyToUrl The string to be checked.
 * @return boolean indicating whether the string is a valid URL.
 */
@Suppress("SwallowedException")
fun isValidUrl(replyToUrl: String): Boolean {
  return try {
    URL(replyToUrl)
    true
  } catch (e: Exception) {
    false
  }
}