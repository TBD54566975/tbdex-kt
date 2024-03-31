package tbdex.sdk.httpserver.handlers

import ServerTest
import TestData.aliceDid
import TestData.createClose
import TestData.createRfq
import assertk.assertThat
import assertk.assertions.contains
import de.fxlae.typeid.TypeId
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tbdex.sdk.httpserver.models.ErrorResponse
import tbdex.sdk.httpserver.models.ExchangesApi
import tbdex.sdk.httpserver.models.SubmitCloseCallback
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.CloseData
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.assertContains
import kotlin.test.assertEquals

class SubmitCloseTest : ServerTest() {

  @Nested
  inner class SubmitCloseServerTest : ServerTest() {
    @Test
    fun `returns BadRequest if no request body is provided`() = runBlocking {
      val response = client.put("/exchanges/123") {
        contentType(ContentType.Application.Json)
      }

      val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

      assertEquals(HttpStatusCode.BadRequest, response.status)
      assertContains(errorResponse.errors.first().detail, "Parsing of TBDex message failed")
    }

    @Test
    fun `returns Conflict if close has a different protocol version than the rest of the exchange`() = runBlocking {
      val rfq = createRfq()
      rfq.sign(aliceDid)
      exchangesApi.addMessage(rfq)

      val close = createClose(exchangeId = rfq.metadata.exchangeId, protocol = "2.0")
      close.sign(aliceDid)

      val response = client.put("/exchanges/${close.metadata.exchangeId}") {
        contentType(ContentType.Application.Json)
        setBody(close)
      }

      val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

      assertEquals(HttpStatusCode.Conflict, response.status)
      assertThat(errorResponse.errors.first().detail).contains("Protocol mismatch")
    }

    @Test
    fun `returns Conflict if close is not allowed based on exchange state`() = runBlocking {
      val close = createClose(TypeId.generate(MessageKind.rfq.name).toString())
      close.sign(aliceDid)
      exchangesApi.addMessage(close)

      val response = client.put("/exchanges/${close.metadata.exchangeId}") {
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
      val close = createClose(TypeId.generate(MessageKind.rfq.name).toString())
      close.sign(aliceDid)

      val response = client.put("/exchanges/${close.metadata.exchangeId}") {
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

      val response = client.put("/exchanges/${close.metadata.exchangeId}") {
        contentType(ContentType.Application.Json)
        setBody(close)
      }

      assertEquals(HttpStatusCode.Accepted, response.status)
    }

    @Test
    fun `returns BadRequest if request body is not a valid Close`() = runBlocking {
      val rfq = createRfq()
      rfq.sign(aliceDid)
      exchangesApi.addMessage(rfq)

      val close = createClose(rfq.metadata.exchangeId)
      close.sign(aliceDid)

      val response = client.put("/exchanges/${close.metadata.exchangeId}") {
        contentType(ContentType.Application.Json)
        setBody("hehe")
      }

      val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

      assertEquals(HttpStatusCode.BadRequest, response.status)
      assertContains(errorResponse.errors.first().detail, "Parsing of TBDex message failed")

    }
  }

  @Nested
  inner class SubmitCloseMockkTest {
    private lateinit var applicationCall: ApplicationCall
    private lateinit var exchangesApi: ExchangesApi
    private val callback: SubmitCloseCallback = mockk(relaxed = true)
    private val exchangeId = TypeId.generate("rfq").toString()
    private val close: Close = Close.create(
      to = "did:ex:pfi",
      from = "did:ex:alice",
      exchangeId = exchangeId,
      closeData = CloseData(
        reason = "test"
      )
    )

    @BeforeEach
    fun setUp() {
      applicationCall = mockk(relaxed = true)
      exchangesApi = mockk(relaxed = true)
    }

    @Test
    fun `verify callback is invoked upon successful close submission`() = runBlocking {
      val messageList = listOf<Message>(mockk(relaxed = true))
      coEvery { exchangesApi.getExchange(exchangeId, any<String>()) } returns messageList

      coEvery { messageList.first().metadata.protocol } returns "1.0"
      coEvery { messageList.last().validNext } returns setOf(MessageKind.close)

      submitClose(applicationCall, exchangesApi, callback, close)

      coVerify(exactly = 1) { callback.invoke(applicationCall, close) }
      coVerify { applicationCall.respond(HttpStatusCode.Accepted) }

    }

    @Test
    fun `verify http accepted is returned if callback is null`() = runBlocking {
      val messageList = listOf<Message>(mockk(relaxed = true))
      coEvery { exchangesApi.getExchange(exchangeId, any<String>()) } returns messageList

      coEvery { messageList.first().metadata.protocol } returns "1.0"
      coEvery { messageList.last().validNext } returns setOf(MessageKind.close)

      submitClose(applicationCall, exchangesApi, null, close)

      coVerify { applicationCall.respond(HttpStatusCode.Accepted) }

    }

    @Test
    fun `verify submitClose fails if getExchange throws NoSuchElementException `() = runBlocking {
      coEvery { exchangesApi.getExchange(exchangeId, any<String>()) } throws NoSuchElementException("nope sorry")

      submitClose(applicationCall, exchangesApi, callback, close)

      coVerify { applicationCall.respond(HttpStatusCode.NotFound, any<ErrorResponse>()) }

    }

    @Test
    fun `verify submitClose fails if getExchange throws Exception `() = runBlocking {
      coEvery { exchangesApi.getExchange(exchangeId, any<String>()) } throws Exception("boo")

      submitClose(applicationCall, exchangesApi, callback, close)

      coVerify { applicationCall.respond(HttpStatusCode.InternalServerError, any<ErrorResponse>()) }

    }

    @Test
    fun `verify submitClose fails if protocol value is inconsistent`() = runBlocking {
      val messageList = listOf<Message>(mockk(relaxed = true))
      coEvery { exchangesApi.getExchange(exchangeId, any<String>()) } returns messageList

      coEvery { messageList.first().metadata.protocol } returns "not 1.0"

      submitClose(applicationCall, exchangesApi, callback, close)

      coVerify { applicationCall.respond(HttpStatusCode.Conflict, any<ErrorResponse>()) }

    }

    @Test
    fun `verify submitClose fails if next valid message is not Close`() = runBlocking {
      val messageList = listOf<Message>(mockk(relaxed = true))
      coEvery { exchangesApi.getExchange(exchangeId, any<String>()) } returns messageList

      coEvery { messageList.first().metadata.protocol } returns "1.0"
      coEvery { messageList.last().validNext } returns setOf(MessageKind.rfq)

      submitClose(applicationCall, exchangesApi, callback, close)

      coVerify { applicationCall.respond(HttpStatusCode.Conflict, any<ErrorResponse>()) }

    }


  }
}