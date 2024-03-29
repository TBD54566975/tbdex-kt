package tbdex.sdk.httpserver.handlers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.httpserver.models.BalancesApi
import tbdex.sdk.httpserver.models.CallbackError
import tbdex.sdk.httpserver.models.ErrorResponse
import tbdex.sdk.httpserver.models.GetCallback
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
suspend fun getBalances(
  call: ApplicationCall,
  balancesApi: BalancesApi,
  callback: GetCallback?
) {

  val balances = balancesApi.getBalances()

  if (callback == null) {
    call.respond(HttpStatusCode.OK, GetBalancesResponse(data = balances))
    return
  }

  try {
    callback.invoke(call, null)
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