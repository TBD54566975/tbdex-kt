package tbdex.sdk.httpserver.handlers

import ServerTest
import TestData.aliceDid
import TestData.createOrder
import TestData.createQuote
import TestData.createRfq
import TestData.pfiDid
import assertk.assertThat
import assertk.assertions.contains
import de.fxlae.typeid.TypeId
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import tbdex.sdk.httpclient.models.ErrorResponse
import tbdex.sdk.protocol.serialization.Json
import java.time.OffsetDateTime
import kotlin.test.assertContains
import kotlin.test.assertEquals

class SubmitOrderTest : ServerTest() {
  @Test
  fun `returns BadRequest if no request body is provided`() = runBlocking {
    val response = client.post("/exchanges/123") {
      contentType(ContentType.Application.Json)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.BadRequest, response.status)
    assertContains(errorResponse.errors.first().detail, "Parsing of TBDex message failed")
  }

  @Test
  fun `returns NotFound if exchange doesn't exist `() = runBlocking {
    val order = createOrder(TypeId.generate("rfq").toString())
    order.sign(aliceDid)

    val response = client.post("/exchanges/${order.metadata.exchangeId}") {
      contentType(ContentType.Application.Json)
      setBody(order)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.NotFound, response.status)
    assertContains(errorResponse.errors.first().detail, "Could not find exchange")
  }

  @Test
  fun `returns Conflict if order is not allowed based on the exchange's current state`() = runBlocking {
    val rfq = createRfq()
    rfq.sign(aliceDid)
    exchangesApi.addMessage(rfq)

    val order = createOrder(rfq.metadata.exchangeId)
    order.sign(aliceDid)

    val response = client.post("/exchanges/${rfq.metadata.exchangeId}") {
      contentType(ContentType.Application.Json)
      setBody(order)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.Conflict, response.status)
    assertContains(
      errorResponse.errors.first().detail,
      "cannot submit Order for an exchange where the last message is kind"
    )
  }

  @Test
  fun `returns Conflict if order has a different protocol version than the rest of the exchange`() = runBlocking {
    val rfq = createRfq()
    rfq.sign(aliceDid)
    exchangesApi.addMessage(rfq)

    val order = createOrder(exchangeId = rfq.metadata.exchangeId, protocol = "2.0")
    order.sign(aliceDid)

    val response = client.post("/exchanges/${order.metadata.exchangeId}") {
      contentType(ContentType.Application.Json)
      setBody(order)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.Conflict, response.status)
    assertThat(errorResponse.errors.first().detail).contains("Protocol mismatch")
  }

  @Test
  fun `returns Forbidden if quote has expired`() = runBlocking {
    val quote = createQuote(expiresAt = OffsetDateTime.now().minusDays(1))
    quote.sign(aliceDid)
    exchangesApi.addMessage(quote)

    val order = createOrder(quote.metadata.exchangeId)
    order.sign(aliceDid)

    val response = client.post("/exchanges/${quote.metadata.exchangeId}") {
      contentType(ContentType.Application.Json)
      setBody(order)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.Forbidden, response.status)
    assertContains(errorResponse.errors.first().detail, "quote is expired")
  }

  @Test
  fun `returns Accepted if order is accepted`() = runBlocking {
    val rfq = createRfq()
    rfq.sign(aliceDid)
    exchangesApi.addMessage(rfq)

    val quote = createQuote(exchangeId = rfq.metadata.exchangeId)
    quote.sign(pfiDid)
    exchangesApi.addMessage(quote)

    val order = createOrder(quote.metadata.exchangeId)
    order.sign(aliceDid)

    val response = client.post("/exchanges/${rfq.metadata.exchangeId}") {
      contentType(ContentType.Application.Json)
      setBody(order)
    }

    assertEquals(HttpStatusCode.Accepted, response.status)
  }
}