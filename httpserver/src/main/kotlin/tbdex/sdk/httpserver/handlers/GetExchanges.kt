package tbdex.sdk.httpserver.handlers

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.httpserver.models.ErrorResponse
import tbdex.sdk.httpserver.models.ExchangesApi
import tbdex.sdk.httpserver.models.GetCallback
import tbdex.sdk.httpserver.models.GetExchangesFilter
import tbdex.sdk.protocol.models.Message

class GetExchangesResponse(
  val data: List<List<Message>>?
)
suspend fun getExchanges(
  call: ApplicationCall,
  exchangesApi: ExchangesApi,
  callback: GetCallback?
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

  // todo: verify JWT token using new CryptoUtils.verify() method
  // to be written to address these issues:
  // generating JWT token: https://github.com/TBD54566975/tbdex-kt/issues/121
  // verifying JWT token: https://github.com/TBD54566975/tbdex/issues/210

  val exchanges = exchangesApi.getExchanges()

  if (callback != null) {
    // TODO: figure out what to do with callback result. should we pass through the exchanges we've fetched
    //       and allow the callback to modify what's returned? (issue #10)
    val result = callback.invoke(call, GetExchangesFilter())
  }

  call.respond(HttpStatusCode.OK, GetExchangesResponse(data = exchanges))
}