package tbdex.sdk.httpclient

import ErrorDetail
import ErrorResponse
import GetExchangeResponse
import GetOfferingsResponse
import SendMessageResponse
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import tbdex.sdk.httpclient.models.GetExchangesFilter
import tbdex.sdk.protocol.Json.jsonMapper
import tbdex.sdk.protocol.models.Rfq
import typeid.TypeID
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.DidIonManager
import web5.sdk.dids.DidKey
import java.net.HttpURLConnection
import kotlin.test.Test

/**
 * Real tbdex client test
 *
 * @constructor Create empty Real tbdex client test
 */
class RealTbdexClientTest {
  private lateinit var server: MockWebServer
  private val pfi = DidIonManager.create(InMemoryKeyManager())
  private val alice = DidKey.create(InMemoryKeyManager())

  @Suppress("MaximumLineLength")
  private val ionDid =
    "did:ion:EiBwNQC_lRp1cAOZJC3XmtCXzSIL_rr0JOVYG82ORwVE_g:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJkd24tc2lnIiwicHVibGljS2V5SndrIjp7ImNydiI6IkVkMjU1MTkiLCJrdHkiOiJPS1AiLCJ4IjoiaTZjbnN1SDRKVEJNWEtic2VnMjhIaTN3NFhwMTNFODVVd25TVzNaZ1lrOCJ9LCJwdXJwb3NlcyI6WyJhdXRoZW50aWNhdGlvbiJdLCJ0eXBlIjoiSnNvbldlYktleTIwMjAifV0sInNlcnZpY2VzIjpbeyJpZCI6InBmaSIsInNlcnZpY2VFbmRwb2ludCI6Imh0dHA6Ly9sb2NhbGhvc3Q6OTAwMCIsInR5cGUiOiJQRkkifV19fV0sInVwZGF0ZUNvbW1pdG1lbnQiOiJFaUNSNlB0MGY3SkRORVdqaFlsazBOdWtJSVFOMGVyc3ZfdHJLeERKdTlmZHZRIn0sInN1ZmZpeERhdGEiOnsiZGVsdGFIYXNoIjoiRWlBLTEzYUNoMmRrcW9oOWIxWDZudnppSXI2VS1JYUpJTFd3UU5sZjJNczBkZyIsInJlY292ZXJ5Q29tbWl0bWVudCI6IkVpQlhoX2dsN1pCd2JUOGNHOU5LT0FSV0xSUzZWUjRRVm44OWEyWldtdzhHOXcifX0"

  @BeforeEach
  fun setup() {
    server = MockWebServer()
    server.start(9000) // doing this because ^ iondid resolves to localhost:9000
  }

  @Test
  @Disabled
  fun `can get offerings`() {
    val resp =
      RealTbdexClient.getOfferings(ionDid)
  }

  @Test
  @Disabled
  fun `can send message`() {
    val message = TestData.getRfq(ionDid, TypeID("offering"))
    message.sign(alice)
    val resp =
      RealTbdexClient.sendMessage(message)
  }

  @Test
  @Disabled
  fun `can get exchange`() {
    val rfq = TestData.getRfq()
    val resp =
      RealTbdexClient.getExchange(ionDid, rfq.metadata.exchangeId.toString(), alice)
  }

  @Test
  @Disabled
  fun `can get exchanges`() {
    val rfq = TestData.getRfq()
    val rfq2 = TestData.getRfq()
    val resp =
      RealTbdexClient.getExchanges(
        pfiDid = ionDid,
        did = alice,
        filter = GetExchangesFilter(listOf(rfq.metadata.exchangeId.toString(), rfq2.metadata.exchangeId.toString()))
      )

  }

  @Test
  fun `get offerings success via mockwebserver`() {
    val mockOfferings = listOf(TestData.getOffering(TestData.getPresentationDefinition()))
    val mockResponseString = jsonMapper.writeValueAsString(mapOf("data" to mockOfferings))
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_OK))

    val response = RealTbdexClient.getOfferings(ionDid, null)

    assertEquals(HttpURLConnection.HTTP_OK, response.status)
    assertTrue(response is GetOfferingsResponse)
    assertEquals("my fake offering", (response as GetOfferingsResponse).data[0].data.description)
  }

  @Test
  fun `get offerings fail via mockwebserver`() {
    val errorDetails = listOf(ErrorDetail(
      id = "1",
      status = "400",
      code = "INVALID_INPUT",
      title = "Invalid Input",
      detail = "The request input is invalid.",
      source = null,
      meta = null
    ))

    val mockResponseString = jsonMapper.writeValueAsString(errorDetails)
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST))

    val response = RealTbdexClient.getOfferings(ionDid, null)
    assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.status)
    assertTrue(response is ErrorResponse)

  }

  @Test
  fun `send RFQ success via mockwebserver`() {

    server.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_ACCEPTED))

    val rfq = TestData.getRfq(ionDid, TypeID("offering"))
    val response = RealTbdexClient.sendMessage(rfq)
    assertEquals(HttpURLConnection.HTTP_ACCEPTED, response.status)
    assertTrue(response is SendMessageResponse)
  }

  @Test
  fun `send RFQ fail via mockwebserver`() {

    val errorDetails = listOf(ErrorDetail(
      id = "1",
      status = "400",
      code = "INVALID_INPUT",
      title = "Invalid Input",
      detail = "The request input is invalid.",
      source = null,
      meta = null
    ))

    val mockResponseString = jsonMapper.writeValueAsString(errorDetails)
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST))

    val rfq = TestData.getRfq(ionDid, TypeID("offering"))
    val response = RealTbdexClient.sendMessage(rfq)
    assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.status)
    assertTrue(response is ErrorResponse)
  }

  @Test
  fun `get exchange success via mockwebserver`() {

    val offeringId = TypeID("offering")
    val rfq = TestData.getRfq(ionDid, offeringId)
    rfq.sign(alice)
    val quote = TestData.getQuote()
    quote.sign(alice) // todo quote should probably signed by pfiDid but we currently don't have the full pfi did + key
    val exchange = listOf(rfq, quote)
    val mockResponseString = jsonMapper.writeValueAsString(mapOf("data" to exchange))
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_OK))

    val response = RealTbdexClient.getExchange(ionDid, "exchange_1234", alice)
    assertEquals(HttpURLConnection.HTTP_OK, response.status)
    assertTrue(response is GetExchangeResponse)
    assertEquals(offeringId, ((response as GetExchangeResponse).data[0] as Rfq).data.offeringId)
  }

  @AfterEach
  fun teardown() {
    server.shutdown()
  }
}