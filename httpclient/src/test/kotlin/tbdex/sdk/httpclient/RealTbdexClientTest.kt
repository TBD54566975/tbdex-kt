package tbdex.sdk.httpclient

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import tbdex.sdk.httpclient.models.ErrorResponse
import tbdex.sdk.httpclient.models.GetOfferingsResponse
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.DidKey
import kotlin.test.Test


class RealTbdexClientTest {
  private lateinit var server: MockWebServer
  private val did = DidKey.create(InMemoryKeyManager())

  @BeforeEach
  fun setup() {
    server = MockWebServer()
    server.enqueue(MockResponse().setBody("hello, world!"))
    server.enqueue(MockResponse().setBody("sup, bra?"))

    server.start()
    val baseUrl = server.url("")
  }

  @Test
  fun `can get offerings`() {
    val resp = RealTbdexClient.getOfferings(did.uri)
    if (resp.status == 200) {
      resp as GetOfferingsResponse
      println(resp.data[0])
    } else {
      resp as ErrorResponse
      println(resp.errors)
    }
  }

  @AfterEach
  fun teardown() {
    server.shutdown()
  }
}