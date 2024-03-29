package tbdex.sdk.httpclient

import de.fxlae.typeid.TypeId
import junit.framework.TestCase.assertEquals
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.httpclient.models.TbdexResponseException
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.CloseData
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.serialization.Json
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.didcore.Service
import web5.sdk.dids.methods.dht.CreateDidDhtOptions
import web5.sdk.dids.methods.dht.DidDht
import web5.sdk.dids.methods.key.DidKey
import java.net.HttpURLConnection
import kotlin.test.Test

/**
 * Real tbdex client test.
 *
 * @constructor Create empty Real tbdex client test
 */
class TbdexHttpClientTest {
  private lateinit var server: MockWebServer
  private val aliceDid = DidKey.create(InMemoryKeyManager())

  private val pfiDid = DidDht.create(
    InMemoryKeyManager(),
    CreateDidDhtOptions(
      services = listOf(Service("123", "PFI", listOf("http://localhost:9000")))
    )
  )

  @BeforeEach
  fun setup() {
    server = MockWebServer()
    server.start(9000) // doing this because ^ pfiDid resolves to http://localhost:9000
  }

  @AfterEach
  fun tearDown() {
    server.shutdown()
  }

  @Test
  fun `get offerings success via mockwebserver`() {
    val offering = TestData.getOffering(TestData.getPresentationDefinition())
    offering.sign(TestData.PFI_DID)
    val mockOfferings = listOf(offering)
    val mockResponseString = Json.jsonMapper.writeValueAsString(mapOf("data" to mockOfferings))
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_OK))

    val response = TbdexHttpClient.getOfferings(pfiDid.uri, null)

    assertEquals(1, response.size)
  }

  @Test
  fun `get offerings fail via mockwebserver`() {
    val errorDetails = mapOf(
      "errors" to listOf(
        ErrorDetail(
          id = "1",
          status = "400",
          code = "INVALID_INPUT",
          title = "Invalid Input",
          detail = "The request input is invalid.",
          source = null,
          meta = null
        )
      )
    )

    val mockResponseString = Json.jsonMapper.writeValueAsString(errorDetails)
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST))

    assertThrows<TbdexResponseException> { TbdexHttpClient.getOfferings(pfiDid.uri, null) }
  }

  @Test
  fun `get balances success via mockwebserver`() {
    val balance = TestData.getBalance()
    balance.sign(TestData.PFI_DID)
    val mockBalance = listOf(balance)
    val mockResponseString = Json.jsonMapper.writeValueAsString(mapOf("data" to mockBalance))
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_OK))

    val requesterDid = DidDht.create(InMemoryKeyManager())
    val response = TbdexHttpClient.getBalances(pfiDid.uri, requesterDid)

    assertEquals(1, response.size)
  }

  @Test
  fun `get balances fail via mockwebserver`() {
    val errorDetails = mapOf(
      "errors" to listOf(
        ErrorDetail(
          id = "1",
          status = "401",
          code = "Unauthorized",
          title = "Unauthorized",
          detail = "The request is unauthorized.",
          source = null,
          meta = null
        )
      )
    )

    val mockResponseString = Json.jsonMapper.writeValueAsString(errorDetails)
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST))
    val requesterDid = DidDht.create(InMemoryKeyManager())
    assertThrows<TbdexResponseException> { TbdexHttpClient.getBalances(pfiDid.uri, requesterDid) }
  }

  @Test
  fun `createExchange(Rfq) submits RFQ`() {

    server.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_ACCEPTED))

    val rfq = TestData.getRfq(pfiDid.uri)
    assertDoesNotThrow { TbdexHttpClient.createExchange(rfq) }

    val request = server.takeRequest()
    assertEquals(request.method, "POST")
    assertEquals(request.path, "/exchanges")
    assertEquals(
      Json.jsonMapper.readTree(request.body.readUtf8()),
      Json.jsonMapper.readTree(Json.stringify(mapOf("rfq" to rfq)))
    )
  }

  @Test
  fun `createExchange(Rfq, replyTo) submits RFQ and replyTo`() {
    server.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_ACCEPTED))

    val rfq = TestData.getRfq(pfiDid.uri)
    val replyTo = "https://tbdex.io/callback"
    assertDoesNotThrow { TbdexHttpClient.createExchange(rfq, replyTo) }

    val request = server.takeRequest()
    assertEquals(request.method, "POST")
    assertEquals(request.path, "/exchanges")
    assertEquals(
      Json.jsonMapper.readTree(request.body.readUtf8()),
      Json.jsonMapper.readTree(Json.stringify(mapOf("rfq" to rfq, "replyTo" to replyTo)))
    )
  }

  @Test
  fun `createExchange(Rfq) throws if server returns error`() {
    val errorDetails = mapOf(
      "errors" to listOf(
        ErrorDetail(
          id = "1",
          status = "400",
          code = "INVALID_INPUT",
          title = "Invalid Input",
          detail = "The request input is invalid.",
          source = null,
          meta = null
        )
      )
    )

    val mockResponseString = Json.jsonMapper.writeValueAsString(errorDetails)
    server
      .enqueue(
        MockResponse()
          .setBody(mockResponseString)
          .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
      )

    val rfq = TestData.getRfq(pfiDid.uri)
    val exception = assertThrows<TbdexResponseException> {
      TbdexHttpClient.createExchange(rfq)
    }
    assertEquals(1, exception.errors?.size)
    assertEquals("400", exception.errors?.get(0)?.status)
  }

  @Test
  fun `createExchange(Rfq, replyTo) throws if server responds with error`() {
    val errorDetails = mapOf(
      "errors" to listOf(
        ErrorDetail(
          id = "1",
          status = "400",
          code = "INVALID_INPUT",
          title = "Invalid Input",
          detail = "The request input is invalid.",
          source = null,
          meta = null
        )
      )
    )

    val mockResponseString = Json.jsonMapper.writeValueAsString(errorDetails)
    server
      .enqueue(
        MockResponse()
          .setBody(mockResponseString)
          .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
      )

    val rfq = TestData.getRfq(pfiDid.uri)
    val exception = assertThrows<TbdexResponseException> {
      TbdexHttpClient.createExchange(rfq, "https://tbdex.io/callback")
    }
    assertEquals(1, exception.errors?.size)
    assertEquals("400", exception.errors?.get(0)?.status)
  }

  @Test
  fun `submitOrder(Order) submits Order`() {
    server.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_ACCEPTED))

    val order = Order.create(
      to = pfiDid.uri,
      from = this.aliceDid.uri,
      exchangeId = TypeId.generate("rfq").toString()
    )
    order.sign(aliceDid)
    assertDoesNotThrow { TbdexHttpClient.submitOrder(order) }

    val request = server.takeRequest()
    assertEquals(request.method, "PUT")
    assertEquals(request.path, "/exchanges/${order.metadata.exchangeId}")
    assertEquals(
      Json.jsonMapper.readTree(request.body.readUtf8()),
      Json.jsonMapper.readTree(Json.stringify(order))
    )
  }

  @Test
  fun `submitOrder(Order) throws if server responds with error`() {
    val errorDetails = mapOf(
      "errors" to listOf(
        ErrorDetail(
          id = "1",
          status = "400",
          code = "INVALID_INPUT",
          title = "Invalid Input",
          detail = "The request input is invalid.",
          source = null,
          meta = null
        )
      )
    )

    val mockResponseString = Json.jsonMapper.writeValueAsString(errorDetails)
    server
      .enqueue(
        MockResponse()
          .setBody(mockResponseString)
          .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
      )

    val order = Order.create(
      to = pfiDid.uri,
      from = this.aliceDid.uri,
      exchangeId = TypeId.generate("rfq").toString()
    )
    order.sign(aliceDid)
    val exception = assertThrows<TbdexResponseException> {
      TbdexHttpClient.submitOrder(order)
    }
    assertEquals(1, exception.errors?.size)
    assertEquals("400", exception.errors?.get(0)?.status)
  }

  @Test
  fun `submitClose(Close) submits Close`() {
    server.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_ACCEPTED))

    val close = Close.create(
      to = pfiDid.uri,
      from = this.aliceDid.uri,
      exchangeId = TypeId.generate("rfq").toString(),
      closeData = CloseData(reason = "No more TBDex for you")
    )
    close.sign(aliceDid)
    assertDoesNotThrow { TbdexHttpClient.submitClose(close) }

    val request = server.takeRequest()
    assertEquals(request.method, "PUT")
    assertEquals(request.path, "/exchanges/${close.metadata.exchangeId}")
    assertEquals(
      Json.jsonMapper.readTree(request.body.readUtf8()),
      Json.jsonMapper.readTree(Json.stringify(close))
    )
  }

  @Test
  fun `submitClose(Close) throws if server responds with error`() {
    val errorDetails = mapOf(
      "errors" to listOf(
        ErrorDetail(
          id = "1",
          status = "400",
          code = "INVALID_INPUT",
          title = "Invalid Input",
          detail = "The request input is invalid.",
          source = null,
          meta = null
        )
      )
    )

    val mockResponseString = Json.jsonMapper.writeValueAsString(errorDetails)
    server
      .enqueue(
        MockResponse()
          .setBody(mockResponseString)
          .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
      )

    val close = Close.create(
      to = pfiDid.uri,
      from = this.aliceDid.uri,
      exchangeId = TypeId.generate("rfq").toString(),
      closeData = CloseData(reason = "No more TBDex for you")
    )
    close.sign(aliceDid)
    val exception = assertThrows<TbdexResponseException> {
      TbdexHttpClient.submitClose(close)
    }
    assertEquals(1, exception.errors?.size)
    assertEquals("400", exception.errors?.get(0)?.status)
  }

  @Test
  fun `get exchange TypeId overload success via mockwebserver`() {
    val offeringId = TypeId.generate("offering").toString()
    val exchange = listOf(rfq(offeringId), quote())
    val mockResponseString = Json.jsonMapper.writeValueAsString(mapOf("data" to exchange))
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_OK))

    val response = TbdexHttpClient.getExchange(pfiDid.uri, this.aliceDid, TypeId.generate("rfq").toString())

    assertEquals(offeringId, (response[0] as Rfq).data.offeringId)
  }

  @Test
  fun `get exchange success via mockwebserver`() {
    val offeringId = TypeId.generate("offering").toString()
    val exchange = listOf(rfq(offeringId), quote())
    val mockResponseString = Json.jsonMapper.writeValueAsString(mapOf("data" to exchange))
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_OK))

    val response = TbdexHttpClient.getExchange(pfiDid.uri, this.aliceDid, "exchange_1234")

    assertEquals(offeringId, (response[0] as Rfq).data.offeringId)
  }

  @Test
  fun `get exchange fail via mockwebserver`() {

    val errorDetails = mapOf(
      "errors" to listOf(
        ErrorDetail(
          id = "1",
          status = "400",
          code = "INVALID_INPUT",
          title = "Invalid Input",
          detail = "The request input is invalid.",
          source = null,
          meta = null
        )
      )
    )

    val mockResponseString = Json.jsonMapper.writeValueAsString(errorDetails)
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST))

    assertThrows<TbdexResponseException> {
      TbdexHttpClient.getExchange(pfiDid.uri, this.aliceDid, "exchange_1234")
    }
  }

  @Test
  fun `get exchanges success via mockwebserver`() {
    val offeringId = TypeId.generate("offering").toString()
    val exchanges = listOf(listOf(rfq(offeringId), quote()))
    val mockResponseString = Json.jsonMapper.writeValueAsString(mapOf("data" to exchanges))
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_OK))

    val response = TbdexHttpClient.getExchanges(pfiDid.uri, this.aliceDid)

    assertEquals(offeringId, (response[0][0] as Rfq).data.offeringId)
  }

  private fun quote(): Quote {
    val quote = TestData.getQuote()
    quote.sign(TestData.PFI_DID)
    return quote
  }

  private fun rfq(offeringId: String): Rfq {
    val rfq = TestData.getRfq(pfiDid.uri, offeringId)
    rfq.sign(TestData.ALICE_DID)
    return rfq
  }

  @Test
  fun `get exchanges fail via mockwebserver`() {

    val errorDetails = mapOf(
      "errors" to listOf(
        ErrorDetail(
          id = "1",
          status = "400",
          code = "INVALID_INPUT",
          title = "Invalid Input",
          detail = "The request input is invalid.",
          source = null,
          meta = null
        )
      )
    )

    val mockResponseString = Json.jsonMapper.writeValueAsString(errorDetails)
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST))

    assertThrows<TbdexResponseException> { TbdexHttpClient.getExchanges(pfiDid.uri, this.aliceDid) }
  }

  @AfterEach
  fun teardown() {
    server.shutdown()
  }
}