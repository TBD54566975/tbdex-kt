package tbdex.sdk.httpserver

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import tbdex.sdk.httpserver.handlers.submitClose
import tbdex.sdk.httpserver.handlers.submitOrder
import tbdex.sdk.httpserver.handlers.submitRfq
import tbdex.sdk.httpserver.models.*
import tbdex.sdk.protocol.serialization.TypeIdModule
import kotlin.collections.set

/**
 * Main function to start the TBDex HTTP server.
 */
fun main() {

  embeddedServer(Netty, port = 8080) {
    val serverConfig = TbdexHttpServerConfig(
      port = 8080,
    )
    val tbdexServer = TbdexHttpServer(serverConfig)
    tbdexServer.configure(this)
  }.start(wait = true)
}

/**
 * Configuration data for TBDex HTTP server.
 *
 * @property port The port on which the server will listen.
 * @property offeringsApi An optional [OfferingsApi] implementation to use.
 * @property exchangesApi An optional [ExchangesApi] implementation to use.
 */
class TbdexHttpServerConfig(
  val port: Int,
  val offeringsApi: OfferingsApi? = null,
  val exchangesApi: ExchangesApi? = null
)


/**
 * TBDex HTTP server responsible for handling RFQs, orders, and other interactions.
 *
 * @property config The configuration for the server, including port and optional APIs.
 */
class TbdexHttpServer(private val config: TbdexHttpServerConfig) {

  private val offeringsApi = config.offeringsApi ?: FakeOfferingsApi()
  private val exchangesApi = config.exchangesApi ?: FakeExchangesApi()

  private val getCallbacks: MutableMap<String, GetCallback> = mutableMapOf()
  private val submitCallbacks: MutableMap<String, SubmitCallback> = mutableMapOf()
  private var embedded = embeddedServer(Netty, port = config.port) {
    configure(this)
  }

  /**
   * Configures the Ktor application with necessary settings, including content negotiation.
   *
   * @param app The Ktor application to be configured.
   */
  fun configure(app: Application) {
    app.install(ContentNegotiation) {
      jackson {
        registerModule(JavaTimeModule())
        registerModule(TypeIdModule())
        registerKotlinModule()
        findAndRegisterModules()
      }
    }

    app.routing {
      get("/") {
        call.respond(
          HttpStatusCode.OK, "Please use the tbdex protocol to communicate with this server " +
                  "or a suitable library: https://github.com/TBD54566975/tbdex-protocol"
        )
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
            exchangesApi = exchangesApi,
            callback = submitCallbacks.getOrDefault("order", null)
          )
        }

        post("/{exchangeId}/close") {
          submitClose(
            call = call,
            exchangesApi = exchangesApi,
            callback = submitCallbacks.getOrDefault("close", null)
          )
        }

        get {
//          getExchanges(call, exchangesApi, getCallbacks.getOrDefault("exchanges", null))
        }
      }

      get("/offerings") {
//        getOfferings(call, offeringsApi,  getCallbacks.getOrDefault("offerings", null))
      }
    }
  }

  /**
   * Adds a submit callback for a specific message kind (RFQ, order, close, etc.).
   *
   * @param messageKind The type of message for which the callback is registered.
   * @param callback The callback to be invoked when a message of the specified kind is received.
   */
  fun <T : SubmitKind> submit(messageKind: T, callback: SubmitCallback) {
    this.submitCallbacks[messageKind.toString()] = callback
  }

  /**
   * Adds a get callback for a specific resource kind (exchanges, offerings, etc.).
   *
   * @param resourceKind The type of resource for which the callback is registered.
   * @param callback The callback to be invoked when a request for the specified resource is received.
   */
  fun <T : GetKind> get(resourceKind: T, callback: GetCallback) {
    this.getCallbacks[resourceKind.toString()] = callback
  }

  /**
   * Starts the embedded Netty server.
   */
  fun start() {
    embedded.start(wait = true)
  }

  /**
   * Stops the embedded Netty server.
   */
  fun stop() {
    embedded.stop(1000, 5000)
  }
}