import Json.objectMapper
import crypto.Crypto
import models.Message
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.Date

class SendMessageOptions(
  val message: Message
)

sealed class Result

class HttpResponse(
  val status: Int,
  val headers: Headers
) : Result()

class ErrorResponse(
  val status: Int,
  val headers: Headers,
  val errors: List<ErrorDetail>?
  ) : Result()

class ErrorResponseBody(
  val errors: List<ErrorDetail>?
)
class ErrorDetail(
  val id: String?,
  val status: String?,
  val code: String?,
  val title: String?,
  val detail: String,
  val source: Source?,
  val meta: Map<String, Any?>?
)

class Source (
  val pointer: String?,
  val parameter: String?,
  val header: String?
)
class TbdexHttpClient {

  companion object {
    // todo: add MessageKind generic typing?
    fun sendMessage(opt: SendMessageOptions) : Result {

      val message = opt.message
      Message.verify(message)

      val pfiDid = message.metadata.to
      val exchangeId = message.metadata.exchangeId
      val kind = message.metadata.kind

      val pfiServiceEndpoint = getPfiServiceEndpoint(pfiDid)
      val url = "${pfiServiceEndpoint}/exchanges/${exchangeId}/${kind}"

      val client = OkHttpClient()
      val JSON = "application/json; charset=utf-8".toMediaType()
      val body = Json.stringify(message).toRequestBody(JSON)

      val request = Request.Builder()
        .url(url)
        .addHeader("Content-Type", "application/json")
        .post(body)
        .build()

      val response = runCatching {
        client.newCall(request).execute()
      }.getOrElse {
        throw Exception("Failed to send message to ${pfiDid}. Error: ${it.message}")
      }

      return if (response.code == 202) {
        HttpResponse(response.code, response.headers)
      } else {
        val responseBody = objectMapper.readValue(response.body!!.string(), ErrorResponseBody::class.java)
        ErrorResponse(response.code, response.headers, responseBody.errors)
      }
    }

    private fun getPfiServiceEndpoint(pfiDid: String): Any {
      TODO("Not yet implemented")
    }
  }

    fun generateRequestToken(): String {
      val timestampToken = Date().toString()
      return Crypto.sign(payload = timestampToken, privateKeyJwk =  "privateKeyJwk", kid = "kid")
    }
  }