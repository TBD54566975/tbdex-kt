package tbdex.sdk.httpserver

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import tbdex.sdk.httpserver.models.ExchangesApi
import tbdex.sdk.httpserver.models.FakeExchangesApi
import tbdex.sdk.httpserver.models.FakeOfferingsApi
import tbdex.sdk.httpserver.models.Filter
import tbdex.sdk.httpserver.models.GetCallback
import tbdex.sdk.httpserver.models.OfferingsApi
import tbdex.sdk.httpserver.models.SubmitCallback
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.httpserver.handlers.submitRfq

fun main() {
  val serverConfig = TbdexHttpServerConfig(
    port = 8080,
    getCallbacks = mapOf(
      "exchanges" to { applicationCall: ApplicationCall, filter: Filter -> },
      "offerings" to { applicationCall: ApplicationCall, filter: Filter -> }
    ),
    submitCallbacks = mapOf(
      "rfq" to { applicationCall: ApplicationCall, messageKind: MessageKind, offering: Offering? -> },
      "order" to { applicationCall: ApplicationCall, messageKind: MessageKind, _: Offering? -> },
      "close" to { applicationCall: ApplicationCall, messageKind: MessageKind, _: Offering? -> }

    )
  )

  val serverWrapper = TbdexHttpServer(serverConfig)
  serverWrapper.start()

  // Do other operations...

  serverWrapper.stop()
}

data class TbdexHttpServerConfig(
  val port: Int,
  val getCallbacks: Map<String, GetCallback>,
  val submitCallbacks: Map<String, SubmitCallback>,
  val offeringsApi: OfferingsApi? = null,
  val exchangesApi: ExchangesApi? = null
)

class TbdexHttpServer(private val config: TbdexHttpServerConfig) {

  private val offeringsApi = config.offeringsApi ?: FakeOfferingsApi()
  private val exchangesApi = config.exchangesApi ?: FakeExchangesApi()

  //  private val callbackFunctions = config.callbackFunctions
  private val getCallbacks = config.getCallbacks
  private val submitCallbacks = config.submitCallbacks
  private val server = embeddedServer(Netty, port = config.port) {
    module()
  }

  private fun Application.module() {
    install(ContentNegotiation) {
      jackson {
        registerModule(JavaTimeModule())
        registerKotlinModule()
        findAndRegisterModules()
      }
    }

    routing {
      get("/") {
        call.respondText {
          "Please use the tbdex protocol to communicate with this server " +
            "or a suitable library: https://github.com/TBD54566975/tbdex-protocol"
        }
      }

      route("/exchanges") {
        post("/{exchangeId}/rfq") {
          submitRfq(
            call = call,
            offeringsApi = offeringsApi,
            exchangesApi = exchangesApi,
            callback = submitCallbacks.getOrDefault("rfq", null)
          )
        }

        post("/{exchangeId}/order") {
          submitOrder(
            call = call,
            offeringsApi = offeringsApi,
            exchangesApi = exchangesApi,
            callback = submitCallbacks.getOrDefault("order", null)
          )
        }

        post("/{exchangeId}/close") {
          submitClose(
            call = call,
            offeringsApi = offeringsApi,
            exchangesApi = exchangesApi,
            callback = submitCallbacks.getOrDefault("close", null)
          )
        }

        get {
          val callback = getCallbacks.getOrDefault("exchanges", null)
          getExchanges(callback)
        }
      }

      get("/offerings") {
        val callback = getCallbacks.getOrDefault("offerings", null)
        getOfferings(callback)
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