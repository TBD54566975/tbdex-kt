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
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tbdex.sdk.httpclient.models.ErrorResponse
import tbdex.sdk.httpserver.models.Callbacks
import tbdex.sdk.httpserver.models.ExchangesApi
import tbdex.sdk.httpserver.models.SubmitCloseCallback
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.CloseData
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.Ignore
import kotlin.test.assertContains
import kotlin.test.assertEquals

class SubmitMessageTest {

  @Nested
  inner class SubmitMessageServerTest : ServerTest() {
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

  @Nested
  inner class SubmitMessageMockkTest {
    private lateinit var applicationCall: ApplicationCall
    private lateinit var exchangesApi: ExchangesApi
    private val callback: Callbacks = Callbacks()
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
    @Ignore
    fun `verify callback is invoked upon successful rfq submission`() = runBlocking {
      // todo ApplicationCall.receiveText() is an extension function
      //  need to find another way to mock it
      coEvery { applicationCall.parameters["exchangeId"] } returns close.metadata.exchangeId
      coEvery { applicationCall.receiveText() } returns Json.stringify(close)

      coVerify(exactly = 1) { submitClose(applicationCall, exchangesApi, any<SubmitCloseCallback>(), close)}

    }
  }

}