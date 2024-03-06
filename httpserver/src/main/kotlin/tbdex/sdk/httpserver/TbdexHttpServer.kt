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
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import tbdex.sdk.httpserver.handlers.createExchange
import tbdex.sdk.httpserver.handlers.getExchanges
import tbdex.sdk.httpserver.handlers.getOfferings
import tbdex.sdk.httpserver.handlers.submitClose
import tbdex.sdk.httpserver.handlers.submitOrder
import tbdex.sdk.httpserver.models.CreateExchangeCallback
import tbdex.sdk.httpserver.models.ExchangesApi
import tbdex.sdk.httpserver.models.FakeExchangesApi
import tbdex.sdk.httpserver.models.FakeOfferingsApi
import tbdex.sdk.httpserver.models.GetExchangesCallback
import tbdex.sdk.httpserver.models.GetOfferingsCallback
import tbdex.sdk.httpserver.models.OfferingsApi
import tbdex.sdk.httpserver.models.SubmitCloseCallback
import tbdex.sdk.httpserver.models.SubmitOrderCallback
import tbdex.sdk.httpserver.models.TbdexHttpServerCallbacks

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
  val exchangesApi: ExchangesApi? = null
)


/**
 * TBDex HTTP server responsible for handling RFQs, orders, and other interactions.
 *
 * @property config The configuration for the server, including port and optional APIs.
 */
class TbdexHttpServer(private val config: TbdexHttpServerConfig) {
  private val callbacks = TbdexHttpServerCallbacks()
  private var embedded = embeddedServer(Netty, port = config.port) {
    configure(this)
  }

  private val pfiDid = config.pfiDid ?: "did:ex:pfi"
  internal val offeringsApi = config.offeringsApi ?: FakeOfferingsApi()
  internal val exchangesApi = config.exchangesApi ?: FakeExchangesApi()

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
          HttpStatusCode.OK, "Please use the tbdex protocol to communicate with this server " +
                  "or a suitable library: https://github.com/TBD54566975/tbdex-protocol"
        )
      }

      route("/exchanges") {
        post("/{exchangeId}") {
          createExchange(
            call = call,
            offeringsApi = offeringsApi,
            exchangesApi = exchangesApi,
            callback = callbacks.onCreateExchange
          )
        }

        post("/{exchangeId}/order") {
          submitOrder(
            call = call,
            exchangesApi = exchangesApi,
            callback = callbacks.onSubmitOrder
          )
        }

        post("/{exchangeId}/close") {
          submitClose(
            call = call,
            exchangesApi = exchangesApi,
            callback = callbacks.onSubmitClose
          )
        }

        get {
          getExchanges(
            call,
            exchangesApi,
            callbacks.onGetExchanges,
            pfiDid)
        }
      }

      get("/offerings") {
        getOfferings(call, offeringsApi,  callbacks.onGetOfferings)
      }
    }
  }

  /**
   * Set a callback or overwrite the existing callback for the CreateExchange endpoint
   * @param callback A callback to be invoked when a valid Rfq is sent to the
   *                 CreateExchange endpoint.
   */
  fun onCreateExchange(callback: CreateExchangeCallback) {
    this.callbacks.onCreateExchange = callback
  }

  /**
   * Set a callback or overwrite the existing callback for the SubmitOrder endpoint
   * @param callback A callback to be invoked when a valid Order is sent to the
   *                 SubmitOrder endpoint.
   */
  fun onSubmitOrder(callback: SubmitOrderCallback) {
    this.callbacks.onSubmitOrder = callback
  }

  /**
   * Set a callback or overwrite the existing callback for the SubmitClose endpoint
   * @param callback A callback to be invoked when a valid Close is sent to the
   *                 SubmitClose endpoint.
   */
  fun onSubmitClose(callback: SubmitCloseCallback) {
    this.callbacks.onSubmitClose = callback
  }

  /**
   * Set up a callback or overwrite the existing callback for the GetExchanges endpoint
   * @param callback A callback to be invoked when a valid request is sent to the
   *                 GetExchanges endpoint.
   */
  fun onGetExchanges(callback: GetExchangesCallback) {
    this.callbacks.onGetExchanges = callback
  }

  /**
   * Set up a callback or overwrite the existing callback for the GetOfferings endpoint
   * @param callback A callback to be invoked when a valid request is sent to the
   *                 GetOfferings endpoint.
   */
  fun onGetOfferings(callback: GetOfferingsCallback) {
    this.callbacks.onGetOfferings = callback
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