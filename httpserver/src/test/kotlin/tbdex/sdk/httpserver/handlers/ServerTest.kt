package tbdex.sdk.httpserver.handlers

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import io.ktor.server.testing.testApplication
import tbdex.sdk.httpserver.TbdexHttpServer
import tbdex.sdk.httpserver.TbdexHttpServerConfig
import tbdex.sdk.protocol.serialization.TypeIdModule

open class ServerTest {
  lateinit var client: HttpClient

  protected fun tbdexServer(block: suspend () -> Unit) = testApplication {
    application {
      val serverConfig = TbdexHttpServerConfig(
        port = 8080,
      )
      val tbdexServer = TbdexHttpServer(serverConfig)
      tbdexServer.configure(this)
    }

    this@ServerTest.client = createClient {
      install(ContentNegotiation) {
        jackson {
          registerModule(JavaTimeModule())
          registerModules(TypeIdModule())
          registerKotlinModule()
          findAndRegisterModules()
        }
      }
    }

    block.invoke()
  }
}