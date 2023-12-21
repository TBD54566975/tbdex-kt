package tbdex.sdk.httpserver.handlers

import ServerTest
import TestData.aliceDid
import TestData.createRfq
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import tbdex.sdk.httpclient.models.ErrorResponse
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.assertContains
import kotlin.test.assertEquals

class SubmitRfqTest : ServerTest() {
  @Test
  fun `returns BadRequest if no request body is provided`() = runBlocking {
    val response = client.post("/exchanges/123/rfq") {
      contentType(ContentType.Application.Json)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.BadRequest, response.status)
    assertContains(errorResponse.errors.first().detail, "Parsing of TBDex message failed")
  }

  @Test
  fun `returns Conflict if rfq already exists for a given exchangeId`() = runBlocking {
    val rfq = createRfq()
    rfq.sign(aliceDid)
    exchangesApi.addMessage(rfq)

    val response = client.post("/exchanges/123/rfq") {
      contentType(ContentType.Application.Json)
      setBody(rfq)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.Conflict, response.status)
    assertContains(errorResponse.errors.first().detail, "RFQ already exists.")
  }

  @Test
  fun `returns BadRequest if rfq does not fit offering requirements`() = runBlocking {
    val rfq = createRfq()
    rfq.sign(aliceDid)

    val response = client.post("/exchanges/123/rfq") {
      contentType(ContentType.Application.Json)
      setBody(rfq)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.BadRequest, response.status)
    assertContains(errorResponse.errors.first().detail, "Failed to verify offering requirements")
  }

  @Test
  fun `returns Accepted if rfq is accepted`() = runBlocking {
    val rfq = createRfq(offeringsApi.getOffering())
    rfq.sign(aliceDid)

    val response = client.post("/exchanges/123/rfq") {
      contentType(ContentType.Application.Json)
      setBody(rfq)
    }

    assertEquals(HttpStatusCode.Accepted, response.status)
  }
}