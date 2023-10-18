package tbdex.sdk.httpclient

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.DidKey


class RealTbdexClientTest {
  private lateinit var server: MockWebServer
  private val did = DidKey.create(InMemoryKeyManager())

  @BeforeEach
  fun setup() {
    server = MockWebServer()
    server.enqueue(MockResponse().setBody("hello, world!"))
    server.enqueue(MockResponse().setBody("sup, bra?"))

    server.start()
//    val baseUrl = server.url("")
  }

//  @Test
//  fun `can get offerings`() {
//    val resp = RealTbdexClient.getOfferings("did:ion:EiBwNQC_lRp1cAOZJC3XmtCXzSIL_rr0JOVYG82ORwVE_g:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJkd24tc2lnIiwicHVibGljS2V5SndrIjp7ImNydiI6IkVkMjU1MTkiLCJrdHkiOiJPS1AiLCJ4IjoiaTZjbnN1SDRKVEJNWEtic2VnMjhIaTN3NFhwMTNFODVVd25TVzNaZ1lrOCJ9LCJwdXJwb3NlcyI6WyJhdXRoZW50aWNhdGlvbiJdLCJ0eXBlIjoiSnNvbldlYktleTIwMjAifV0sInNlcnZpY2VzIjpbeyJpZCI6InBmaSIsInNlcnZpY2VFbmRwb2ludCI6Imh0dHA6Ly9sb2NhbGhvc3Q6OTAwMCIsInR5cGUiOiJQRkkifV19fV0sInVwZGF0ZUNvbW1pdG1lbnQiOiJFaUNSNlB0MGY3SkRORVdqaFlsazBOdWtJSVFOMGVyc3ZfdHJLeERKdTlmZHZRIn0sInN1ZmZpeERhdGEiOnsiZGVsdGFIYXNoIjoiRWlBLTEzYUNoMmRrcW9oOWIxWDZudnppSXI2VS1JYUpJTFd3UU5sZjJNczBkZyIsInJlY292ZXJ5Q29tbWl0bWVudCI6IkVpQlhoX2dsN1pCd2JUOGNHOU5LT0FSV0xSUzZWUjRRVm44OWEyWldtdzhHOXcifX0")
//    if (resp.status == 200) {
//      resp as GetOfferingsResponse
//      println(resp.data[0])
//    } else {
//      resp as ErrorResponse
//      println(resp.errors)
//    }
//  }
//
//  @Test
//  fun testGetOfferingsSuccess() {
//    val mockOfferings = TestData.getOffering(listOf(TestData.getPresentationDefinition()))
//    val mockResponseString = objectMapper.writeValueAsString(mapOf("data" to mockOfferings))
//    server.enqueue(MockResponse().setBody(mockResponseString).setResponseCode(HttpURLConnection.HTTP_OK))
//
//    val response = RealTbdexClient.getOfferings("someDid", null)
//
//    assertEquals(HttpURLConnection.HTTP_OK, response.status)
//    assertTrue(response is GetOfferingsResponse)
//  }

  @AfterEach
  fun teardown() {
    server.shutdown()
  }
}