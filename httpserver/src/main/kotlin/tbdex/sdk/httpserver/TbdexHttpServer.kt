package tbdex.sdk.httpserver

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import tbdex.sdk.httpserver.handlers.createExchange
import tbdex.sdk.httpserver.handlers.getBalances
import tbdex.sdk.httpserver.handlers.getExchange
import tbdex.sdk.httpserver.handlers.getExchanges
import tbdex.sdk.httpserver.handlers.getOfferings
import tbdex.sdk.httpserver.handlers.submitMessage
import tbdex.sdk.httpserver.models.BalancesApi
import tbdex.sdk.httpserver.models.Callbacks
import tbdex.sdk.httpserver.models.CreateExchangeCallback
import tbdex.sdk.httpserver.models.ExchangesApi
import tbdex.sdk.httpserver.models.FakeBalancesApi
import tbdex.sdk.httpserver.models.FakeExchangesApi
import tbdex.sdk.httpserver.models.FakeOfferingsApi
import tbdex.sdk.httpserver.models.GetExchangeCallback
import tbdex.sdk.httpserver.models.GetExchangesCallback
import tbdex.sdk.httpserver.models.GetOfferingsCallback
import tbdex.sdk.httpserver.models.OfferingsApi
import tbdex.sdk.httpserver.models.SubmitCloseCallback
import tbdex.sdk.httpserver.models.SubmitOrderCallback

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
  val pfiDid: String? = null,
  val offeringsApi: OfferingsApi? = null,
  val exchangesApi: ExchangesApi? = null,
  val balancesApi: BalancesApi? = null
)


/**
 * TBDex HTTP server responsible for handling RFQs, orders, and other interactions.
 *
 * @property config The configuration for the server, including port and optional APIs.
 */
class TbdexHttpServer(private val config: TbdexHttpServerConfig) {
  private val callbacks = Callbacks()
  private var embedded = embeddedServer(Netty, port = config.port) {
    configure(this)
  }

  internal val pfiDid = config.pfiDid ?: "did:ex:pfi"
  internal val offeringsApi = config.offeringsApi ?: FakeOfferingsApi()
  internal val exchangesApi = config.exchangesApi ?: FakeExchangesApi()
  internal val balancesApi = config.balancesApi ?: FakeBalancesApi()

  /**
   * Configures the Ktor application with necessary settings, including content negotiation.
   *
   * @param app The Ktor application to be configured.
   */
  fun configure(app: Application) {
    app.install(ContentNegotiation) {
      jackson {
        registerModule(JavaTimeModule())
        registerKotlinModule()
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
        findAndRegisterModules()
      }
    }

    app.routing {
      get("/") {
        call.respond(
          HttpStatusCode.OK, "Please use the tbdex protocol " +
          "via a suitable library to communicate with this server: " +
          "https://github.com/TBD54566975/tbdex"
        )
      }

      get("/offerings") {
        getOfferings(
          call,
          offeringsApi,
          callbacks.getOfferings
        )
      }

      get("/balances") {
        getBalances(
          call,
          balancesApi,
          callbacks.getBalances,
          pfiDid
        )
      }

      route("/exchanges") {
        post {
          createExchange(
            call = call,
            offeringsApi = offeringsApi,
            exchangesApi = exchangesApi,
            callback = callbacks.createExchange
          )
        }

        put("/{exchangeId}") {
          submitMessage(
            call = call,
            exchangesApi = exchangesApi,
            callbacks = callbacks
          )
        }

        get {
          getExchanges(
            call,
            exchangesApi,
            callbacks.getExchanges,
            pfiDid
          )
        }

        get("/{exchangeId}") {
          getExchange(
            call,
            exchangesApi,
            callbacks.getExchange,
            pfiDid
          )
        }
      }

    }
  }

  /**
   * Adds a GetOfferingsCallback for handling requests for offerings.
   *
   * @param callback GetOfferingsCallback function to be registered
   */
  fun onGetOfferings(callback: GetOfferingsCallback) {
    callbacks.getOfferings = callback
  }

  /**
   * Adds a GetExchangesCallback for handling requests for exchanges.
   *
   * @param callback GetExchangesCallback function to be registered
   */
  fun onGetExchanges(callback: GetExchangesCallback) {
    callbacks.getExchanges = callback
  }

  /**
   * Adds a GetExchangeCallback for handling requests for an exchange with a specific exchangeId.
   *
   * @param callback GetExchangeCallback function to be registered
   */
  fun onGetExchange(callback: GetExchangeCallback) {
    callbacks.getExchange = callback
  }

  /**
   * Adds a CreateExchangeCallback for handling requests to create an exchange.
   *
   * @param callback CreateExchangeCallback function to be registered
   */
  fun onCreateExchange(callback: CreateExchangeCallback) {
    callbacks.createExchange = callback
  }

  /**
   * Adds a SubmitOrderCallback for handling requests to submit an order.
   *
   * @param callback SubmitOrderCallback function to be registered
   */
  fun onSubmitOrder(callback: SubmitOrderCallback) {
    callbacks.submitOrder = callback
  }

  /**
   * Adds a SubmitOrderCallback for handling requests to submit an order.
   *
   * @param callback SubmitOrderCallback function to be registered
   */
  fun onSubmitClose(callback: SubmitCloseCallback) {
    callbacks.submitClose = callback
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