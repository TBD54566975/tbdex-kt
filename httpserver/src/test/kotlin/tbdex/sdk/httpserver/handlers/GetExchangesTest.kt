package tbdex.sdk.httpserver.handlers

import ServerTest
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class GetExchangesTest : ServerTest() {

  @Test
  fun `returns 200 if exchanges are found`() = runBlocking {

    val response = client.get("/exchanges")

  }

}