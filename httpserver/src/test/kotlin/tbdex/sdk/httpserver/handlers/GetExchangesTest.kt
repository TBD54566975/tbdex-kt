package tbdex.sdk.httpserver.handlers

import ServerTest
import TestData
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import tbdex.sdk.httpclient.RequestToken
import tbdex.sdk.httpclient.models.ErrorResponse
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class GetExchangesTest : ServerTest() {

  @BeforeEach
  fun `setup client`() {
    client.config {
      install(ContentNegotiation) {
        jackson {
          registerModule(JavaTimeModule())
          registerKotlinModule()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
          findAndRegisterModules()
        }
      }
      install(Auth) {
        bearer {
          sendWithoutRequest { true }
        }
      }
    }
  }

  @Test
  fun `returns 401 if no Bearer token is present`() = runBlocking {

    val response = client.get("/exchanges")

    assertEquals(HttpStatusCode.Unauthorized, response.status)

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)
    assertContains(errorResponse.errors.first().detail, "Authorization header required")
  }

  @Test
  fun `returns 401 if malformed Bearer token is present`() = runBlocking {

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
    rfq.sign(TestData.aliceDid)
    val quote = TestData.createQuote()
    quote.sign(TestData.pfiDid)

    exchangesApi.addMessage(rfq)
    exchangesApi.addMessage(quote)

    val response = client.get("/exchanges?id=${rfq.metadata.exchangeId}&id=${quote.metadata.exchangeId}") {
      bearerAuth(RequestToken.generate(TestData.aliceDid, TestData.pfiDid.uri))
    }

    assertEquals(HttpStatusCode.OK, response.status)

    val responseString = response.bodyAsText()
    val jsonNode = Json.jsonMapper.readTree(responseString)
    val exchanges = jsonNode.get("data").elements().asSequence()
      .map { exchange ->
        exchange.elements().asSequence().map {
          val string = it.toString()
          Message.parse(string)
        }.toList()
      }
      .toList()

    assertEquals(exchanges.size, 2)
    assertEquals(exchanges[0].size, 1)

    assertEquals(exchanges[0][0].metadata.id, rfq.metadata.id)
    assertEquals(exchanges[1][0].metadata.id, quote.metadata.id)
  }
}