package tbdex.sdk.httpclient

import GetOfferingsResponse
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import tbdex.sdk.httpclient.Json.objectMapper
import tbdex.sdk.httpclient.models.GetExchangesFilter
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
//    server = MockWebServer()
//    server.enqueue(MockResponse().setBody("hello, world!"))
//    server.enqueue(MockResponse().setBody("sup, bra?"))
//
//    server.start()
//    val baseUrl = server.url("")
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
    val message = TestData.getRfq(ionDid)
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
  @Disabled
  fun testGetOfferingsSuccess() {
    val mockOfferings = TestData.getOffering(TestData.getPresentationDefinition())
    val mockResponseString = objectMapper.writeValueAsString(mapOf("data" to mockOfferings))
    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_OK))

    val response = RealTbdexClient.getOfferings("someDid", null)

    assertEquals(HttpURLConnection.HTTP_OK, response.status)
    assertTrue(response is GetOfferingsResponse)
  }

  @AfterEach
  fun teardown() {
//    server.shutdown()
  }
}