package tbdex.sdk.httpserver.handlers

import ServerTest
import TestData
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import tbdex.sdk.httpclient.models.ErrorResponse
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class GetExchangesTest : ServerTest() {

  @Test
  fun `returns 401 if no Bearer token is present`() = runBlocking {

    client.config {
      install(Auth) {
        bearer {
          sendWithoutRequest { true }
        }
      }
    }
    val response = client.get("/exchanges")

    assertEquals(HttpStatusCode.Unauthorized, response.status)

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)
    assertContains(errorResponse.errors.first().detail, "Authorization header required")
  }

  @Test
  fun `returns 401 if malformed Bearer token is present`() = runBlocking {

    client.config {
      install(Auth) {
        bearer {
          sendWithoutRequest { true }
        }
      }
    }
    val response = client.get("/exchanges") {
      bearerAuth("Bearer abc123")
    }

    assertEquals(HttpStatusCode.Unauthorized, response.status)

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)
    assertContains(errorResponse.errors.first().detail, "Malformed Authorization header.")
  }

  @Test
  fun `returns 200 if exchanges are found`() = runBlocking {
    val rfq = TestData.createRfq()
    val aliceDid = rfq.metadata.from
    val quote = TestData.createQuote()
    exchangesApi.addMessage(rfq)
    exchangesApi.addMessage(quote)

    client.config {
      install(Auth) {
        bearer {
          sendWithoutRequest { true }
        }
      }
    }
    val response = client.get("/exchanges") {
      bearerAuth("abc123")
    }

    assertEquals(HttpStatusCode.OK, response.status)

    // todo: currently failing to map value to GetExchangesResponse since Message is a sealed class with abstract fields
    val exchangesResponse = Json.jsonMapper.readValue(response.bodyAsText(), GetExchangesResponse::class.java)
    assertEquals(((exchangesResponse.data?.get(0) as List<Message>)[0] as Rfq).metadata.from, aliceDid)

  }

}