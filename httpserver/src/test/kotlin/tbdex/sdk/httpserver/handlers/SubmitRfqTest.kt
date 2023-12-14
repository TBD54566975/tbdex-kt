package tbdex.sdk.httpserver.handlers

import io.ktor.server.testing.TestApplication
import io.ktor.server.testing.testApplication
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.After
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import tbdex.sdk.httpserver.TbdexHttpServer
import tbdex.sdk.httpserver.TbdexHttpServerConfig

class SubmitRfqTest {
  val api = TbdexHttpServer(TbdexHttpServerConfig(8080))
  val client = OkHttpClient()

  @After
  fun teardown() {
    api.stop()
  }

  @Test
  fun `returns 400 if no request body is provided`() {
    api.start()

    val request = Request.Builder()
      .url("http://localhost:8000/exchanges/123/rfq")
      .post("".toRequestBody())
      .build()

    val response = client.newCall(request).execute()
  }

//  @Test
//  fun `start server`() {
//    testApplication {
//      application {
//        module()
//      }
//    }(api.server) {
//      handleRequest(HttpMethod.Post, "/").apply {
//        assertContains("Please use the tbdex protocol", response.content!!)
//      }
//    }
//  }

  companion object {
    lateinit var testApp: TestApplication

    @JvmStatic
    @BeforeAll
    fun setup() {
      testApp = TestApplication {
        testApplication {
          TbdexHttpServer.module
        }
      }
    }

    @JvmStatic
    @AfterAll
    fun teardown() {
      testApp.stop()
    }
  }
}