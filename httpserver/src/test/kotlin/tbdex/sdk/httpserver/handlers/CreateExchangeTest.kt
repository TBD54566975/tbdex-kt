package tbdex.sdk.httpserver.handlers

import ServerTest
import TestData.aliceDid
import TestData.createRfq
import de.fxlae.typeid.TypeId
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tbdex.sdk.httpclient.models.CreateExchangeRequest
import tbdex.sdk.httpclient.models.ErrorResponse
import tbdex.sdk.httpserver.models.CreateExchangeCallback
import tbdex.sdk.httpserver.models.ExchangesApi
import tbdex.sdk.httpserver.models.OfferingsApi
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.OfferingData
import tbdex.sdk.protocol.models.PayinDetails
import tbdex.sdk.protocol.models.PayinMethod
import tbdex.sdk.protocol.models.PayoutDetails
import tbdex.sdk.protocol.models.PayoutMethod
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.models.RfqData
import tbdex.sdk.protocol.models.SelectedPayinMethod
import tbdex.sdk.protocol.models.SelectedPayoutMethod
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.Ignore
import kotlin.test.assertContains
import kotlin.test.assertEquals

class CreateExchangeTest {

  @Nested
  inner class CreateExchangeServerTest : ServerTest() {
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

  @Nested
  inner class CreateExchangeMockkTest {
    private lateinit var applicationCall: ApplicationCall
    private lateinit var offeringsApi: OfferingsApi
    private lateinit var exchangesApi: ExchangesApi
    private val callback: CreateExchangeCallback = mockk(relaxed = true)
    private val exchangeId = TypeId.generate("rfq").toString()
    private val offeringId = TypeId.generate("offering").toString()
    private val rfq: Rfq = Rfq.create(
      to = "did:ex:pfi",
      from = "did:ex:alice",
      rfqData = RfqData(
        offeringId = offeringId,
        payin = SelectedPayinMethod(
          kind = "BTC_ADDRESS",
          paymentDetails = mapOf(),
          amount = "0.001"
        ),
        payout = SelectedPayoutMethod(
          kind = "ETH_ADDRESS",
          paymentDetails = mapOf()
        ),
        claims = listOf()
      )
    )
    private val offering = Offering.create(
      from = "did:ex:pfi",
      data = OfferingData(
        description = "test offering",
        payoutUnitsPerPayinUnit = "1",
        payin = PayinDetails(
          currencyCode = "BTC",
          min = "0.0001",
          max = "10",
          methods = listOf(
            PayinMethod(
              kind = "BTC_ADDRESS",
              requiredPaymentDetails = null
            )
          )
        ),
        payout = PayoutDetails(
          currencyCode = "ETH",
          methods = listOf(
            PayoutMethod(
              kind = "ETH_ADDRESS",
              requiredPaymentDetails = null,
              estimatedSettlementTime = 10
            )
          )
        ),
        requiredClaims = null
      )
    )

    @BeforeEach
    fun setUp() {
      applicationCall = mockk(relaxed = true)
      offeringsApi = mockk(relaxed = true)
      exchangesApi = mockk(relaxed = true)
    }

    @Test
    @Ignore
    fun `verify callback is invoked upon successful rfq submission`() = runBlocking {
      val messageList = listOf<Message>(mockk(relaxed = true))
      val rfqJson = """{"rfq":"${Json.stringify(rfq)}"}"""
      // todo ApplicationCall.receiveText() is an extension function
      //  need to find another way to mock it
      coEvery { applicationCall.receiveText() } returns rfqJson

      coEvery { exchangesApi.getExchange(exchangeId, any<String>()) } returns messageList
      coEvery { offeringsApi.getOffering(any()) } returns offering

      coEvery { messageList.first().metadata.protocol } returns "1.0"
      coEvery { messageList.last().validNext } returns setOf(MessageKind.close)

      createExchange(applicationCall, offeringsApi, exchangesApi, callback)

      coVerify(exactly = 1) { callback.invoke(applicationCall, rfq, offering, null) }
      coVerify { applicationCall.respond(HttpStatusCode.Accepted) }

    }

  }
}