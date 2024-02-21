package tbdex.sdk.httpclient

import de.fxlae.typeid.TypeId
import junit.framework.TestCase.assertEquals
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import tbdex.sdk.httpclient.models.CreateExchangeRequest
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.httpclient.models.TbdexResponseException
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.serialization.Json
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.methods.ion.CreateDidIonOptions
import web5.sdk.dids.methods.ion.DidIon
import web5.sdk.dids.methods.ion.models.Service
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
  private val alice = DidKey.create(InMemoryKeyManager())

  private val pfiDid = DidIon.create(
    InMemoryKeyManager(),
    CreateDidIonOptions(
      servicesToAdd = listOf(Service("123", "PFI", "http://localhost:9000"))
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
  fun `send RFQ without replyTo success via mockwebserver`() {

    server.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_ACCEPTED))

    val rfq = TestData.getRfq(pfiDid.uri, TypeId.generate("offering"))
    assertDoesNotThrow { TbdexHttpClient.sendMessage(rfq) }

    val request1 = server.takeRequest()
    assertEquals(request1.path, "/exchanges/${rfq.metadata.exchangeId}")
    assertEquals(
      Json.jsonMapper.readTree(request1.body.readUtf8()),
      Json.jsonMapper.readTree(Json.stringify(mapOf("rfq" to rfq)))
    )
  }

  @Test
  fun `send RFQ with replyTo success via mockwebserver`() {

    server.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_ACCEPTED))

    val rfq = TestData.getRfq(pfiDid.uri, TypeId.generate("offering"))
    assertDoesNotThrow { TbdexHttpClient.sendMessage(rfq, "https://tbdex.io/callback") }
  }

  @Test
  fun `send RFQ with createExchange success via mockwebserver`() {

    server.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_ACCEPTED))

    val rfq = TestData.getRfq(pfiDid.uri, TypeId.generate("offering"))
    assertDoesNotThrow { TbdexHttpClient.createExchange(rfq, "https://tbdex.io/callback") }
  }

  @Test
  fun `send RFQ fail via mockwebserver`() {
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

    val rfq = TestData.getRfq(pfiDid.uri, TypeId.generate("offering"))
    val exception = assertThrows<TbdexResponseException> {
      TbdexHttpClient.sendMessage(rfq)
    }
    assertEquals(1, exception.errors?.size)
    assertEquals("400", exception.errors?.get(0)?.status)
  }

  @Test
  fun `send RFQ with createExchange() fail via mockwebserver`() {
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

    val rfq = TestData.getRfq(pfiDid.uri, TypeId.generate("offering"))
    val exception = assertThrows<TbdexResponseException> {
      TbdexHttpClient.createExchange(rfq, "https://tbdex.io/callback")
    }
    assertEquals(1, exception.errors?.size)
    assertEquals("400", exception.errors?.get(0)?.status)
  }

  @Test
  fun `get exchange TypeId overload success via mockwebserver`() {
    val offeringId = TypeId.generate("offering")
    val exchange = listOf(rfq(offeringId), quote())
    val mockResponseString = Json.jsonMapper.writeValueAsString(mapOf("data" to exchange))
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_OK))

    val response = TbdexHttpClient.getExchange(pfiDid.uri, alice, TypeId.generate("rfq"))

    assertEquals(offeringId, (response[0] as Rfq).data.offeringId)
  }

  @Test
  fun `get exchange success via mockwebserver`() {
    val offeringId = TypeId.generate("offering")
    val exchange = listOf(rfq(offeringId), quote())
    val mockResponseString = Json.jsonMapper.writeValueAsString(mapOf("data" to exchange))
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_OK))

    val response = TbdexHttpClient.getExchange(pfiDid.uri, alice, "exchange_1234")

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
      TbdexHttpClient.getExchange(pfiDid.uri, alice, "exchange_1234")
    }
  }

  @Test
  fun `get exchanges success via mockwebserver`() {
    val offeringId = TypeId.generate("offering")
    val exchanges = listOf(listOf(rfq(offeringId), quote()))
    val mockResponseString = Json.jsonMapper.writeValueAsString(mapOf("data" to exchanges))
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_OK))

    val response = TbdexHttpClient.getExchanges(pfiDid.uri, alice)

    assertEquals(offeringId, (response[0][0] as Rfq).data.offeringId)
  }

  private fun quote(): Quote {
    val quote = TestData.getQuote()
    quote.sign(TestData.PFI_DID)
    return quote
  }

  private fun rfq(offeringId: TypeId): Rfq {
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

    assertThrows<TbdexResponseException> { TbdexHttpClient.getExchanges(pfiDid.uri, alice) }
  }

  @AfterEach
  fun teardown() {
    server.shutdown()
  }
}