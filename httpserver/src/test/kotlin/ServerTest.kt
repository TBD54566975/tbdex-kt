import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import tbdex.sdk.httpserver.TbdexHttpServer
import tbdex.sdk.httpserver.TbdexHttpServerConfig
import tbdex.sdk.httpserver.models.FakeExchangesApi
import tbdex.sdk.protocol.serialization.TypeIdModule

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class ServerTest {
  lateinit var client: HttpClient
  lateinit var exchangesApi: FakeExchangesApi

  private val tbdexApplication = TestApplication {
    application {
      val serverConfig = TbdexHttpServerConfig(
        port = 8080,
      )
      val tbdexServer = TbdexHttpServer(serverConfig)
      tbdexServer.configure(this)
      this@ServerTest.exchangesApi = tbdexServer.exchangesApi as FakeExchangesApi
    }
  }

  @BeforeAll
  fun setup() {
    tbdexApplication.start()

    this.client = tbdexApplication.createClient {
      install(ContentNegotiation) {
        jackson {
          registerModule(JavaTimeModule())
          registerModules(TypeIdModule())
          registerKotlinModule()
          findAndRegisterModules()
        }
      }
    }
  }

  @AfterEach
  fun reset() {
    this.exchangesApi.resetExchanges()
  }

  @AfterAll
  fun teardown() {
    this.client.close()
    tbdexApplication.stop()
  }
}