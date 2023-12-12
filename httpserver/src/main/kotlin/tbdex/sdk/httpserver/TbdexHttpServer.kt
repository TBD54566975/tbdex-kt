package tbdex.sdk.httpserver

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import tbdex.sdk.httpserver.models.TbdexCallback

fun main() {
  val serverConfig = TbdexHttpServerConfig(
    port = 8080,
    callbackFunctions = mapOf(
      "rfq" to { println("Callback function for RFQ endpoint") },
    )
  )

  val serverWrapper = TbdexHttpServer(serverConfig)
  serverWrapper.start()

  // Do other operations...

  serverWrapper.stop()
}

data class TbdexHttpServerConfig(
  val port: Int,
  val callbackFunctions: Map<String, () -> Unit>
)

class TbdexHttpServer(private val config: TbdexHttpServerConfig) {
  private val callbackFunctions = config.callbackFunctions
  private val server = embeddedServer(Netty, port = config.port) {
    install(ContentNegotiation) {
      jackson {
        registerModule(JavaTimeModule())
        registerKotlinModule()
        findAndRegisterModules()
      }
    }

    routing {
      route("/exchanges") {
        post("/{exchangeId}/rfq") {
          val request = call.receive<RfqRequest>()
          val callback =
          val response = submitRfq(request, callback)
          call.respond(response)
        }

        post("/{exchangeId}/order") {
          val request = call.receive<OrderRequest>()
          val callback =
          val response = submitOrder(request, callback)
          call.respond(response)
        }

        post("/{exchangeId}/close") {
          val request = call.receive<CloseRequest>()
          val callback =
          val response = submitClose(request, callback)
          call.respond(response)
        }

        get {
          val callback = call.receive<TbdexCallback>()
          val response = getExchanges(callback)
          call.respond(response)
        }
      }

      get("/offerings") {
        val response = getOfferings(callback)
        call.respond(response)
      }
    }
  }

  fun start() {
    server.start(wait = true)
  }

  fun stop() {
    server.stop(1000, 5000)
  }
}