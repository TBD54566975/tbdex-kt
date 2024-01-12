package tbdex.sdk.httpserver.handlers

import ServerTest
import TestData.aliceDid
import TestData.createClose
import TestData.createRfq
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
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.Ignore
import kotlin.test.assertContains
import kotlin.test.assertEquals

class SubmitCloseTest : ServerTest() {
  @Test
  fun `returns BadRequest if no request body is provided`() = runBlocking {
    val response = client.post("/exchanges/123/close") {
      contentType(ContentType.Application.Json)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.BadRequest, response.status)
    assertContains(errorResponse.errors.first().detail, "Parsing of TBDex message failed")
  }

  @Test
  fun `returns Conflict if close is not allowed based on exchange state`() = runBlocking {
    val close = createClose(TypeId.generate(MessageKind.close.name))
    close.sign(aliceDid)
    exchangesApi.addMessage(close)

    val response = client.post("/exchanges/123/close") {
      contentType(ContentType.Application.Json)
      setBody(close)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.Conflict, response.status)
    assertContains(
      errorResponse.errors.first().detail,
      "cannot submit Order for an exchange where the last message is kind"
    )
  }

  @Test
  fun `returns NotFound if exchange doesn't exist`() = runBlocking {
    val close = createClose(TypeId.generate(MessageKind.close.name))
    close.sign(aliceDid)

    val response = client.post("/exchanges/123/close") {
      contentType(ContentType.Application.Json)
      setBody(close)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.NotFound, response.status)
    assertContains(
      errorResponse.errors.first().detail,
      "Could not find exchange"
    )
  }

  @Test
  fun `returns Accepted if close is accepted`() = runBlocking {
    val rfq = createRfq()
    rfq.sign(aliceDid)
    exchangesApi.addMessage(rfq)

    val close = createClose(rfq.metadata.exchangeId)
    close.sign(aliceDid)

    val response = client.post("/exchanges/123/close") {
      contentType(ContentType.Application.Json)
      setBody(close)
    }

    assertEquals(HttpStatusCode.Accepted, response.status)
  }

  @Test
  @Ignore
  fun `returns BadRequest if request body is not a valid Close`() = runBlocking { }
}