package tbdex.sdk.httpclient

import tbdex.sdk.protocol.Json
import tbdex.sdk.protocol.models.* // TODO don't use * imports
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class TbdexHttpClient {
  companion object {
    private val client = OkHttpClient()

    fun sendMessage(opts: SendMessageOptions<out MessageKind>): HttpResponse {
//      Message.verify(jsonMessage)

      val pfiDid = opts.message.metadata.to
      val exchangeId = opts.message.metadata.exchangeId
      val kind = opts.message.metadata.kind
      val pfiServiceEndpoint = getPfiServiceEndpoint(pfiDid)
      val apiRoute = "$pfiServiceEndpoint/exchanges/$exchangeId/$kind"

      val request = Request.Builder()
        .url(apiRoute)
        .post(Json
          .stringify(opts.message)
          .toRequestBody("application/json; charset=utf-8".toMediaType()))
        .build()

      client.newCall(request).execute().use { response ->
        val headers = response.headers
        val status = response.code

        if (status == 202) {
          return DataResponse(status, headers, response.body)
        } else {
          // todo 'as' casting no bueno
          val responseBody = Json.parse(response.body?.string() as String) as ErrorResponse
          return ErrorResponse(status, headers, null, responseBody.errors)
        }
      }
    }

    // Implement other functions similar to the above
    // ...

    fun getPfiServiceEndpoint(did: String): String {
      // Implementation here
      return "todo"
    }

//    suspend fun generateRequestToken(privateKeyJwk: Web5PrivateKeyJwk, kid: String): String {
//      // Implementation here
//    }
  }

  data class SendMessageOptions<T : MessageKind>(
//    val message: Message<T> or MessageModel<T> // Define your own 'or' type or use a sealed class/interface
    val message: Message
  )

  // Define other options and types
  // ...

  // Define HttpResponseOrErrorResponse as a sealed class or interface
  // ...

  // Define HttpResponse and ErrorResponse
  // ...
}
