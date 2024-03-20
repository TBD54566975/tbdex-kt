package tbdex.sdk.httpserver.handlers;

import ServerTest
import TestData.aliceDid
import TestData.createOrder
import TestData.createQuote
import TestData.pfiDid
import de.fxlae.typeid.TypeId
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import tbdex.sdk.httpclient.models.ErrorResponse
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.assertContains
import kotlin.test.assertEquals

class SubmitMessageTest : ServerTest() {
  @Test
  fun `returns BadRequest if exchangeId of message does not match URL`() = runBlocking {
    val order = createOrder(TypeId.generate("rfq").toString())
    order.sign(aliceDid)
    val response = client.put("/exchanges/1234") {
      contentType(ContentType.Application.Json)
      setBody(order)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.BadRequest, response.status)
    assertContains(errorResponse.errors.first().detail, "Exchange ID of message must match URL")
  }

  @Test
  fun `returns BadRequest if message is not a valid Order or Close`() = runBlocking {
    val quote = createQuote(TypeId.generate("rfq").toString())
    quote.sign(pfiDid)
    val response = client.put("/exchanges/${quote.metadata.exchangeId}") {
      contentType(ContentType.Application.Json)
      setBody(quote)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.BadRequest, response.status)
    assertContains(errorResponse.errors.first().detail, "Message must be a valid Order or Close message")
  }
}