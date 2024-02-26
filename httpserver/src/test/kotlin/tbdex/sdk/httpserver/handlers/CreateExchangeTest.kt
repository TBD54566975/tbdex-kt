package tbdex.sdk.httpserver.handlers

import ServerTest
import TestData.aliceDid
import TestData.createRfq
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import tbdex.sdk.httpclient.models.CreateExchangeRequest
import tbdex.sdk.httpclient.models.ErrorResponse
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.assertContains
import kotlin.test.assertEquals

class CreateExchangeTest : ServerTest() {
  @Test
  fun `returns BadRequest if no request body is provided`() = runBlocking {
    val response = client.post("/exchanges") {
      contentType(ContentType.Application.Json)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.BadRequest, response.status)
    assertContains(errorResponse.errors.first().detail, "Parsing of TBDex createExchange request failed")
  }

  @Test
  fun `returns Conflict if rfq already exists for a given exchangeId`() = runBlocking {
    val rfq = createRfq()
    rfq.sign(aliceDid)
    exchangesApi.addMessage(rfq)

    val response = client.post("/exchanges") {
      contentType(ContentType.Application.Json)
      setBody(CreateExchangeRequest(rfq))
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.Conflict, response.status)
    assertContains(errorResponse.errors.first().detail, "RFQ already exists.")
  }

  @Test
  fun `returns BadRequest if rfq does not fit offering requirements`() = runBlocking {
    val rfq = createRfq(null, listOf("foo"))
    rfq.sign(aliceDid)

    val response = client.post("/exchanges") {
      contentType(ContentType.Application.Json)
      setBody(CreateExchangeRequest(rfq))
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.BadRequest, response.status)
    assertContains(errorResponse.errors.first().detail, "Failed to verify offering requirements")
  }

  @Test
  fun `returns BadRequest if replyTo is an invalid URL`() = runBlocking {
    val rfq = createRfq()
    rfq.sign(aliceDid)

    val response = client.post("/exchanges") {
      contentType(ContentType.Application.Json)
      setBody(CreateExchangeRequest(rfq, "foo"))
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.BadRequest, response.status)
    assertContains(errorResponse.errors.first().detail, "replyTo must be a valid URL")
  }

  @Test
  fun `returns Accepted if rfq is accepted`() = runBlocking {
    val rfq = createRfq(offeringsApi.getOffering("123"))
    rfq.sign(aliceDid)

    val response = client.post("/exchanges") {
      contentType(ContentType.Application.Json)
      setBody(CreateExchangeRequest(rfq, "http://localhost:9000/callback"))
    }

    assertEquals(HttpStatusCode.Accepted, response.status)
  }
}