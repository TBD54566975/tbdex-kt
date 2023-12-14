package tbdex.sdk.httpserver.handlers

import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.testing.testApplication
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import software.amazon.ion.system.IonTextWriterBuilder.json
import tbdex.sdk.httpserver.TbdexHttpServer
import tbdex.sdk.httpserver.TbdexHttpServerConfig
import kotlin.concurrent.thread
import kotlin.test.assertEquals

class SubmitRfqTest {
  val api = embeddedServer(Netty, port = 8080) {
    val serverConfig = TbdexHttpServerConfig(
      port = 8080,
    )
    val tbdexServer = TbdexHttpServer(serverConfig)
    tbdexServer.configure(this)
  }
  val client = OkHttpClient()

//  @After
//  fun teardown() {
//    api.stop()
//  }

  @Test
  fun `returns 400 if no request body is provided`() {
    val request = Request.Builder()
      .url("http://10.0.2.2:8080/exchanges/123/rfq")
      .post("".toRequestBody())
      .build()

    val response = client.newCall(request).execute()
  }

  @Test
  fun `returns 400 if no request body is provided - using testApplication`() {
    testApplication {
      application {
        val serverConfig = TbdexHttpServerConfig(
          port = 8080,
        )
        val tbdexServer = TbdexHttpServer(serverConfig)
        tbdexServer.configure(this)
      }

      val client = createClient {
        install(ContentNegotiation) {
          json()
        }
      }

      val response = client.post("/exchanges/123/rfq") {
        contentType(ContentType.Application.Json)
      }

      assertEquals(HttpStatusCode.BadRequest, response.status)
    }

  }

  companion object {
    private lateinit var server: TbdexHttpServer

    @JvmStatic
    @BeforeAll
    fun setup() {
      thread {
        server = TbdexHttpServer(TbdexHttpServerConfig(8080))
        server.start()
      }
    }

    @JvmStatic
    @AfterAll
    fun teardown() {
      server.stop()
    }
  }
}
//
//  companion object {
//    lateinit var testApp: TestApplication
//
//    @JvmStatic
//    @BeforeAll
//    fun setup() {
//      testApp = TestApplication {
//        testApplication {
//          TbdexHttpServer.module
//        }
//      }
//    }
//
//    @JvmStatic
//    @AfterAll
//    fun teardown() {
//      testApp.stop()
//    }
//  }