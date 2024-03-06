package tbdex.sdk.httpserver.handlers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.httpserver.models.CallbackError
import tbdex.sdk.httpserver.models.ErrorResponse
import tbdex.sdk.httpserver.models.GetOfferingsCallback
import tbdex.sdk.httpserver.models.GetOfferingsFilter
import tbdex.sdk.httpserver.models.OfferingsApi
import tbdex.sdk.protocol.models.Offering

/**
 * Get offerings response
 *
 * @property data list of Offerings
 */
class GetOfferingsResponse(
  val data: List<Offering>?
)

/**
 * Get offerings request handler
 *
 * @param call Ktor server application call
 * @param offeringsApi Offering API interface
 * @param callback Callback function to be invoked
 */
suspend fun getOfferings(
  call: ApplicationCall,
  offeringsApi: OfferingsApi,
  callback: GetOfferingsCallback?
) {
  val queryParams = GetOfferingsFilter(
    id = call.parameters["id"],
    payoutCurrency = call.parameters["payoutCurrency"],
    payinCurrency = call.parameters["payinCurrency"]
  )
  val offerings = offeringsApi.getOfferings(queryParams)

  if (callback == null) {
    call.respond(HttpStatusCode.OK, GetOfferingsResponse(data = offerings))
    return
  }

  try {
    // copied from httpserver-js
    // TODO: figure out what to do with callback result. should we pass through the offerings we've fetched
    // and allow the callback to modify what's returned? (issue #11)
    callback.invoke(call, queryParams)
  } catch (e: CallbackError) {
    call.respond(e.statusCode, ErrorResponse(e.details))
    return
  } catch (e: Exception) {
    val errorDetail = ErrorDetail(detail = e.message ?: "Unknown error while getting Offerings")
    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(listOf(errorDetail)))
    return
  }

  call.respond(HttpStatusCode.OK, GetOfferingsResponse(data = offerings))
}