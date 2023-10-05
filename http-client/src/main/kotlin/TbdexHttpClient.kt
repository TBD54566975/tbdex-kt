import crypto.Crypto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import models.Message
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
  val errors: List<Any>?
  ) : Result()

class Errors(
  val errors: List<Any>?
)
class TbdexHttpClient {

  companion object {
    // todo: add MessageKind generic typing?
    suspend fun sendMessage(opt: SendMessageOptions) : Result {

      val message = opt.message
      Message.verify(message)

      val pfiDid = message.metadata.to
      val exchangeId = message.metadata.exchangeId
      val kind = message.metadata.kind

      val pfiServiceEndpoint = getPfiServiceEndpoint(pfiDid)
      val apiRoute = "${pfiServiceEndpoint}/exchanges/${exchangeId}/${kind}"

      // todo do i need this CIO engine
      val client = HttpClient(CIO)
      val response: io.ktor.client.statement.HttpResponse
        try {
          response = client.post(apiRoute) {
            contentType(ContentType.Application.Json)
            setBody(message)
          }
        } catch (e: Exception) {
          throw Exception("Failed to send message to ${pfiDid}. Error: ${e.message}")
        }

      return if (response.status == HttpStatusCode.Created) {
        HttpResponse(response.status.value, response.headers)
      } else {
        val responseBody = response.body<Errors>()
        ErrorResponse(response.status.value, response.headers, responseBody.errors)
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