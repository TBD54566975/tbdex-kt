package tbdex.sdk.httpserver.handlers

import ServerTest
import TestData.aliceDid
import TestData.createClose
import de.fxlae.typeid.TypeId
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import tbdex.sdk.httpclient.models.ErrorResponse
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.assertContains
import kotlin.test.assertEquals

class SubmitCloseTest : ServerTest() {
  @Test
  fun `returns 400 if no request body is provided`() = runBlocking {
    val response = client.post("/exchanges/123/close") {
      contentType(ContentType.Application.Json)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.BadRequest, response.status)
    assertContains(errorResponse.errors.first().detail, "Parsing of TBDex message failed")
  }

  @Test
  fun `returns 409 if close is not allowed based on exchange state`() = runBlocking {
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
  fun `returns a 400 if request body is not a valid Close`() = runBlocking { }

  @Test
  fun `returns a 404 if exchange doesn't exist`() = runBlocking { }

  @Test
  fun `returns a 202 if close is accepted`() = runBlocking { }
}