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
  fun `returns 409 if rfq already exists for a given exchangeId`() = runBlocking {
    val rfq = createRfq()
    rfq.sign(aliceDid)

    val response = client.post("/exchanges/123/order") {
      contentType(ContentType.Application.Json)
      setBody(rfq)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.Conflict, response.status)
    assertContains(errorResponse.errors.first().detail, "RFQ already exists.")
  }
}