package tbdex.sdk.httpserver

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
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
import tbdex.sdk.httpserver.handlers.submitRfq
import tbdex.sdk.httpserver.models.ExchangesApi
import tbdex.sdk.httpserver.models.FakeExchangesApi
import tbdex.sdk.httpserver.models.FakeOfferingsApi
import tbdex.sdk.httpserver.models.GetCallback
import tbdex.sdk.httpserver.models.OfferingsApi
import tbdex.sdk.httpserver.models.SubmitCallback

fun main() {
  val serverConfig = TbdexHttpServerConfig(
    port = 8080,
  )

  val serverWrapper = TbdexHttpServer(serverConfig)
  serverWrapper.start()

  // Do other operations...

  serverWrapper.stop()
}

data class TbdexHttpServerConfig(
  val port: Int,
  val offeringsApi: OfferingsApi? = null,
  val exchangesApi: ExchangesApi? = null
)

enum class SubmitKind {
  rfq, order, close
}

enum class GetKind {
  exchanges, offerings
}


class TbdexHttpServer(private val config: TbdexHttpServerConfig) {

  private val offeringsApi = config.offeringsApi ?: FakeOfferingsApi()
  private val exchangesApi = config.exchangesApi ?: FakeExchangesApi()

  private val getCallbacks: MutableMap<String, GetCallback> = mutableMapOf()
  private val submitCallbacks: MutableMap<String, SubmitCallback> = mutableMapOf()
  private val server = embeddedServer(Netty, port = config.port) {
    module()
  }

  /**
   * Setup the callback for the available Submit Requests (eg. RFQ, Order, Close)
   * @param messageKind - the kind of message to be handled
   * @param callback - the handler for the message
   */
  fun <T : SubmitKind> submit(messageKind: T, callback: SubmitCallback) {
    this.submitCallbacks[messageKind.toString()] = callback
  }

  /**
   * Setup the callback for the available Get Requests (eg. offerings, exchanges)
   * @param resourceKind - the kind of resource to be handled
   * @param callback - the handler for the resource
   */
  fun <T : GetKind> get(resourceKind: T, callback: GetCallback) {
    this.getCallbacks[resourceKind.toString()] = callback
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