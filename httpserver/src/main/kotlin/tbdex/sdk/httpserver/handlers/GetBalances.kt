package tbdex.sdk.httpserver.handlers

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import tbdex.sdk.httpclient.RequestToken
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.httpserver.models.BalancesApi
import tbdex.sdk.httpserver.models.CallbackError
import tbdex.sdk.httpserver.models.ErrorResponse
import tbdex.sdk.httpserver.models.GetBalancesCallback
import tbdex.sdk.protocol.models.Balance

/**
 * Get balances response
 *
 * @property data list of Offerings
 */
class GetBalancesResponse(
  val data: List<Balance>?
)
/**
 * Get balances request handler
 *
 * @param call Ktor server application call
 * @param balancesApi Balances API interface
 * @param callback Callback function to be invoked
 */
@Suppress("SwallowedException")
suspend fun getBalances(
  call: ApplicationCall,
  balancesApi: BalancesApi,
  callback: GetBalancesCallback?,
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

  val balances = balancesApi.getBalances(requesterDid)

  if (callback == null) {
    call.respond(HttpStatusCode.OK, GetBalancesResponse(data = balances))
    return
  }

  try {
    callback.invoke(call)
  } catch (e: CallbackError) {
    call.respond(e.statusCode, ErrorResponse(e.details))
    return
  } catch (e: Exception) {
    val errorDetail = ErrorDetail(detail = e.message ?: "Unknown error while getting Offerings")
    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(listOf(errorDetail)))
    return
  }

  call.respond(HttpStatusCode.OK, GetBalancesResponse(data = balances))
}