package tbdex.sdk.httpserver.handlers

import ServerTest
import TestData
import TestData.aliceDid
import TestData.createOrder
import de.fxlae.typeid.TypeId
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import tbdex.sdk.httpclient.models.ErrorResponse
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.Ignore
import kotlin.test.assertContains
import kotlin.test.assertEquals

class SubmitOrderTest : ServerTest() {
  @Test
  fun `returns 400 if no request body is provided`() = runBlocking {
    val response = client.post("/exchanges/123/order") {
      contentType(ContentType.Application.Json)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.BadRequest, response.status)
    assertContains(errorResponse.errors.first().detail, "Parsing of TBDex message failed")
  }

  @Test
  fun `returns 404 if exchange doesn't exist `() = runBlocking {
    val order = createOrder(TypeId.generate("rfq"))
    order.sign(aliceDid)

    val response = client.post("/exchanges/123/order") {
      contentType(ContentType.Application.Json)
      setBody(order)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.NotFound, response.status)
    assertContains(errorResponse.errors.first().detail, "Could not find exchange")
  }

  @Test
  fun `returns a 409 if order is not allowed based on the exchange's current state`() = runBlocking {
    val rfq = TestData.createRfq()
    rfq.sign(aliceDid)
    exchangesApi.addMessage(rfq)

    val order = createOrder(rfq.metadata.exchangeId)
    order.sign(aliceDid)

    val response = client.post("/exchanges/123/order") {
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
  fun `returns a 404 if quote is undefined`() = runBlocking {
    val rfq = TestData.createRfq()
    rfq.sign(aliceDid)
    exchangesApi.addMessage(rfq)

    val order = createOrder(rfq.metadata.exchangeId)
    order.sign(aliceDid)

    val response = client.post("/exchanges/123/order") {
      contentType(ContentType.Application.Json)
      setBody(order)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.NotFound, response.status)
    assertContains(errorResponse.errors.first().detail, "quote is undefined")
  }

  @Test
  fun `returns a 400 if quote has expired`() = runBlocking {
    val quote = TestData.createQuote()
    quote.sign(aliceDid)
    exchangesApi.addMessage(quote)

    val order = createOrder(quote.metadata.exchangeId)
    order.sign(aliceDid)

    val response = client.post("/exchanges/123/order") {
      contentType(ContentType.Application.Json)
      setBody(order)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.NotFound, response.status)
    assertContains(errorResponse.errors.first().detail, "quote is undefined")
  }

  @Test
  @Ignore
  fun `returns a 202 if order is accepted`() = runBlocking {}

}