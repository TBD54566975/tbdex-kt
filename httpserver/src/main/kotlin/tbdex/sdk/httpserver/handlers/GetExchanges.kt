package tbdex.sdk.httpserver.handlers

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import tbdex.sdk.httpclient.RequestToken
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.httpserver.models.ErrorResponse
import tbdex.sdk.httpserver.models.ExchangesApi
import tbdex.sdk.httpserver.models.GetExchangesCallback
import tbdex.sdk.httpserver.models.GetExchangesFilter
import tbdex.sdk.protocol.models.Message

/**
 * Get exchanges response
 *
 * @property data list of exchanges (list of tbdex messages)
 */
class GetExchangesResponse(
  val data: List<List<Message>>?
)

/**
 * Get exchanges
 *
 * @param call Ktor server application call
 * @param exchangesApi Exchanges API interface
 * @param callback Callback function to be invoked
 */
@Suppress("SwallowedException")
suspend fun getExchanges(
  call: ApplicationCall,
  exchangesApi: ExchangesApi,
  callback: GetExchangesCallback?,
  pfiDid: String
) {
  val authzHeader = call.request.headers[HttpHeaders.Authorization]
  if (authzHeader == null) {
    call.respond(
      HttpStatusCode.Unauthorized,
      ErrorResponse(
        errors = listOf(
          ErrorDetail(
            detail = "Authorization header required"
          )
        )
      )
    )
    return
  }

  val arr = authzHeader.split("Bearer ")
  if (arr.size != 2) {
    call.respond(
      HttpStatusCode.Unauthorized,
      ErrorResponse(
        errors = listOf(
          ErrorDetail(
            detail = "Malformed Authorization header. Expected: Bearer TOKEN_HERE"
          )
        )
      )
    )
    return
  }

  val token = arr[1]
  val requesterDid: String
  try {
    requesterDid = RequestToken.verify(token, pfiDid)
  } catch (e: Exception) {
    call.respond(
      HttpStatusCode.Unauthorized,
      ErrorResponse(
        errors = listOf(
          ErrorDetail(
            detail = "Could not verify Authorization header."
          )
        )
      )
    )
    return
  }

  val exchanges = exchangesApi.getExchanges()

  if (callback != null) {
    // TODO: figure out what to do with callback result. should we pass through the exchanges we've fetched
    //       and allow the callback to modify what's returned? (issue #10)
    val result = callback.invoke(call, GetExchangesFilter())
  }

  call.respond(HttpStatusCode.OK, GetExchangesResponse(data = exchanges))
}