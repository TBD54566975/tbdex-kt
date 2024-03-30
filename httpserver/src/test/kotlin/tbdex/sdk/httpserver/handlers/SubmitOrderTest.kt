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
import tbdex.sdk.httpserver.models.SubmitOrderCallback
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.QuoteData
import tbdex.sdk.protocol.models.QuoteDetails
import tbdex.sdk.protocol.serialization.Json
import java.time.OffsetDateTime
import kotlin.test.assertContains
import kotlin.test.assertEquals

class SubmitOrderTest {

  @Nested
  inner class SubmitOrderServerTest : ServerTest() {
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
    fun `returns NotFound if exchange doesn't exist `() = runBlocking {
      val order = createOrder(TypeId.generate("rfq").toString())
      order.sign(aliceDid)

      val response = client.put("/exchanges/${order.metadata.exchangeId}") {
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

      val response = client.put("/exchanges/${rfq.metadata.exchangeId}") {
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

      val response = client.put("/exchanges/${order.metadata.exchangeId}") {
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

      val response = client.put("/exchanges/${quote.metadata.exchangeId}") {
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

      val response = client.put("/exchanges/${rfq.metadata.exchangeId}") {
        contentType(ContentType.Application.Json)
        setBody(order)
      }

      assertEquals(HttpStatusCode.Accepted, response.status)
    }
  }

  @Nested
  inner class SubmitOrderMockkTest {

    private lateinit var applicationCall: ApplicationCall
    private lateinit var exchangesApi: ExchangesApi
    private val callback: SubmitOrderCallback = mockk(relaxed = true)
    private val exchangeId = TypeId.generate("rfq").toString()
    private val order: Order = Order.create(
      to = "did:ex:pfi",
      from = "did:ex:alice",
      exchangeId = exchangeId
    )
    private val quote = Quote.create(
      to = "did:ex:alice",
      from = "did:ex:pfi",
      exchangeId = exchangeId,
      quoteData = QuoteData(
        expiresAt = OffsetDateTime.now().plusDays(10),
        payin = QuoteDetails(
          currencyCode = "USD",
          amount = "100.0"
        ),
        payout = QuoteDetails(
          currencyCode = "BTC",
          amount = "0.0005"
        )
      )
    )


    @BeforeEach
    fun setUp() {
      applicationCall = mockk(relaxed = true)
      exchangesApi = mockk(relaxed = true)
    }

    @Test
    fun `verify callback is invoked upon successful order submission`() = runBlocking {
      val messageList = listOf<Message>(mockk(relaxed = true), quote)
      coEvery { exchangesApi.getExchange(exchangeId) } returns messageList

      coEvery { messageList.first().metadata.protocol } returns "1.0"

      submitOrder(applicationCall, exchangesApi, callback, order)

      coVerify(exactly = 1) { callback.invoke(applicationCall, order) }
      coVerify { applicationCall.respond(HttpStatusCode.Accepted) }

    }

    @Test
    fun `verify http accepted is returned when callback is null during order submission`() = runBlocking {
      val messageList = listOf<Message>(mockk(relaxed = true), quote)
      coEvery { exchangesApi.getExchange(exchangeId) } returns messageList

      coEvery { messageList.first().metadata.protocol } returns "1.0"

      submitOrder(applicationCall, exchangesApi, null, order)

      coVerify { applicationCall.respond(HttpStatusCode.Accepted) }

    }

    @Test
    fun `verify submitOrder fails callback is invoked upon successful order submission`() = runBlocking {
      coEvery { exchangesApi.getExchange(exchangeId) } throws NoSuchElementException()

      submitOrder(applicationCall, exchangesApi, callback, order)

      coVerify {
        applicationCall.respond(
          HttpStatusCode.NotFound,
          any<tbdex.sdk.httpserver.models.ErrorResponse>()
        )
      }

    }

    @Test
    fun `verify submitOrder fails if protocol value is inconsistent`() = runBlocking {
      val messageList = listOf<Message>(mockk(relaxed = true))
      coEvery { exchangesApi.getExchange(exchangeId) } returns messageList

      coEvery { messageList.first().metadata.protocol } returns "not 1.0"

      submitOrder(applicationCall, exchangesApi, callback, order)

      coVerify {
        applicationCall.respond(
          HttpStatusCode.Conflict,
          any<tbdex.sdk.httpserver.models.ErrorResponse>()
        )
      }

    }

    @Test
    fun `verify submitOrder fails if next valid message is not Order`() = runBlocking {
      val messageList = listOf<Message>(mockk(relaxed = true), order)
      coEvery { exchangesApi.getExchange(exchangeId) } returns messageList

      coEvery { messageList.first().metadata.protocol } returns "1.0"

      submitOrder(applicationCall, exchangesApi, callback, order)

      coVerify {
        applicationCall.respond(
          HttpStatusCode.Conflict,
          any<tbdex.sdk.httpserver.models.ErrorResponse>()
        )
      }

    }

    @Test
    fun `verify submitOrder fails if quote is expired`() = runBlocking {
      val expiredQuote = Quote.create(
        to = "did:ex:alice",
        from = "did:ex:pfi",
        exchangeId = exchangeId,
        quoteData = QuoteData(
          expiresAt = OffsetDateTime.now().minusDays(10),
          payin = QuoteDetails(
            currencyCode = "USD",
            amount = "100.0"
          ),
          payout = QuoteDetails(
            currencyCode = "BTC",
            amount = "0.0005"
          )
        )
      )

      val messageList = listOf<Message>(mockk(relaxed = true), expiredQuote)
      coEvery { exchangesApi.getExchange(exchangeId) } returns messageList

      coEvery { messageList.first().metadata.protocol } returns "1.0"

      submitOrder(applicationCall, exchangesApi, callback, order)

      coVerify {
        applicationCall.respond(
          HttpStatusCode.Forbidden,
          any<ErrorResponse>()
        )
      }

    }

    @Test
    fun `verify submitOrder fails when callback invocation throws an exception`() = runBlocking {
      val messageList = listOf<Message>(mockk(relaxed = true), quote)
      coEvery { exchangesApi.getExchange(exchangeId) } returns messageList

      coEvery { messageList.first().metadata.protocol } returns "1.0"
      coEvery { callback.invoke(applicationCall, order) } throws Exception("booo")

      submitOrder(applicationCall, exchangesApi, callback, order)

      coVerify {
        applicationCall.respond(
          HttpStatusCode.InternalServerError,
          any<tbdex.sdk.httpserver.models.ErrorResponse>()
        )
      }

    }

  }
}