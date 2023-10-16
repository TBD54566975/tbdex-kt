package tbdex.sdk.httpclient

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import tbdex.sdk.protocol.Json
import tbdex.sdk.protocol.models.* // TODO don't use * imports
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

data class SendMessageOptions<T : MessageKind>(
//    val message: Message<T> or MessageModel<T> // Define your own 'or' type or use a sealed class/interface
  val message: Message
)

data class GetOfferingsOptions(
  val pfiDid: String,
  val filter: Map<String, Any>? = null
)

class TbdexHttpClient {
  companion object {
    private val httpClient = OkHttpClient()

    fun sendMessage(opts: SendMessageOptions<out MessageKind>): HttpResponse {
      // todo verify message

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

      httpClient.newCall(request).execute().use { response ->
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

    fun getOfferings(opts: GetOfferingsOptions): Any {
      val pfiServiceEndpoint = getPfiServiceEndpoint(opts.pfiDid) // Assume this function is defined
      val queryParams = opts.filter?.let { "?" + it.entries.joinToString("&") { "${it.key}=${it.value}" } } ?: ""
      val apiRoute = "$pfiServiceEndpoint/offerings$queryParams"

      val request = Request.Builder()
        .url(apiRoute)
        .build()

      httpClient.newCall(request).execute().use { response ->
        val responseBody = response.body?.string()

        if (responseBody.isNullOrEmpty()) {
          throw Exception("Response cannot be null or empty")
        }

        return if (response.isSuccessful) {
          // todo
          val data: List<Offering> = Json.parse(responseBody) as List<Offering>
          DataResponse(response.code, response.headers, data)
        } else {
          val errors: List<ErrorDetail> = Json.parse(responseBody) as List<ErrorDetail> // Assume ErrorDetail is defined
          ErrorResponse(response.code, response.headers, null, errors)
        }
      }
    }

    fun getPfiServiceEndpoint(did: String): String {
      // todo did:ion not current supported by web5-kt
      // val result = DidResolvers.resolve(did)
      // println(result)
      return "http://localhost:9000"
    }
  }
}
