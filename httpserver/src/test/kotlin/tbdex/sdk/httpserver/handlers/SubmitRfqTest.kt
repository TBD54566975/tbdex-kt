package tbdex.sdk.httpserver.handlers

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import tbdex.sdk.httpserver.TbdexHttpServer
import tbdex.sdk.httpserver.TbdexHttpServerConfig
import kotlin.concurrent.thread

class SubmitRfqTest : TbdexHttpServer() {
  val client = OkHttpClient()

  @Test
  fun `returns 400 if no request body is provided`() {
    val request = Request.Builder()
      .url("http://10.0.2.2:8080/exchanges/123/rfq")
      .post("".toRequestBody())
      .build()

    val response = client.newCall(request).execute()

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