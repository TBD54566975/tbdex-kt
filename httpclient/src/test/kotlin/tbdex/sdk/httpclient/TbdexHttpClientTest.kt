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
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.serialization.Json
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.DidIonManager
import web5.sdk.dids.DidKey
import java.net.HttpURLConnection
import kotlin.test.Test

/**
 * Real tbdex client test.
 *
 * @constructor Create empty Real tbdex client test
 */
class TbdexHttpClientTest {
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

    val response = TbdexHttpClient.getOfferings(ionDid, null)

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

    assertThrows<TbdexResponseException> { TbdexHttpClient.getOfferings(ionDid, null) }
  }

  @Test
  fun `send RFQ success via mockwebserver`() {

    server.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_ACCEPTED))

    val rfq = TestData.getRfq(ionDid, TypeId.generate("offering"))
    assertDoesNotThrow { TbdexHttpClient.sendMessage(rfq) }
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
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST))

    val rfq = TestData.getRfq(ionDid, TypeId.generate("offering"))
    val exception = assertThrows<TbdexResponseException> { TbdexHttpClient.sendMessage(rfq) }
    assertEquals(1, exception.errors?.size)
    assertEquals("400", exception.errors?.get(0)?.status)
  }

  @Test
  fun `get exchange success via mockwebserver`() {
    val offeringId = TypeId.generate("offering")
    val exchange = listOf(rfq(offeringId), quote())
    val mockResponseString = Json.jsonMapper.writeValueAsString(mapOf("data" to exchange))
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_OK))

    val response = TbdexHttpClient.getExchange(ionDid, alice, "exchange_1234")

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

    assertThrows<TbdexResponseException> { TbdexHttpClient.getExchange(ionDid, alice, "exchange_1234") }
  }

  @Test
  fun `get exchanges success via mockwebserver`() {
    val offeringId = TypeId.generate("offering")
    val exchanges = listOf(listOf(rfq(offeringId), quote()))
    val mockResponseString = Json.jsonMapper.writeValueAsString(mapOf("data" to exchanges))
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_OK))

    val response = TbdexHttpClient.getExchanges(ionDid, alice)

    assertEquals(offeringId, (response[0][0] as Rfq).data.offeringId)
  }

  private fun quote(): Quote {
    val quote = TestData.getQuote()
    quote.sign(TestData.PFI_DID)
    return quote
  }

  private fun rfq(offeringId: TypeId): Rfq {
    val rfq = TestData.getRfq(ionDid, offeringId)
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

    assertThrows<TbdexResponseException> { TbdexHttpClient.getExchanges(ionDid, alice) }
  }

  @AfterEach
  fun teardown() {
    server.shutdown()
  }
}