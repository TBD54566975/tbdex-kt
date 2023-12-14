package tbdex.sdk.httpserver

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
import tbdex.sdk.httpserver.handlers.submitRfq
import tbdex.sdk.httpserver.models.ExchangesApi
import tbdex.sdk.httpserver.models.FakeExchangesApi
import tbdex.sdk.httpserver.models.FakeOfferingsApi
import tbdex.sdk.httpserver.models.GetCallback
import tbdex.sdk.httpserver.models.GetKind
import tbdex.sdk.httpserver.models.OfferingsApi
import tbdex.sdk.httpserver.models.SubmitCallback
import tbdex.sdk.httpserver.models.SubmitKind

fun main() {

  embeddedServer(Netty, port = 8080) {
    val serverConfig = TbdexHttpServerConfig(
      port = 8080,
    )
    val tbdexServer = TbdexHttpServer(serverConfig)
    tbdexServer.configure(this)
  }.start(wait = true)

}

class TbdexHttpServerConfig(
  val port: Int,
  val offeringsApi: OfferingsApi? = null,
  val exchangesApi: ExchangesApi? = null
)


class TbdexHttpServer(private val config: TbdexHttpServerConfig) {

  private val offeringsApi = config.offeringsApi ?: FakeOfferingsApi()
  private val exchangesApi = config.exchangesApi ?: FakeExchangesApi()

  private val getCallbacks: MutableMap<String, GetCallback> = mutableMapOf()
  private val submitCallbacks: MutableMap<String, SubmitCallback> = mutableMapOf()
  private var embedded = embeddedServer(Netty, port = config.port) {
    configure(this)
  }

  fun configure(app: Application) {
    app.install(ContentNegotiation) {
      jackson {
        registerModule(JavaTimeModule())
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
//          submitOrder(
//            call = call,
//            exchangesApi = exchangesApi,
//            callback = submitCallbacks.getOrDefault("order", null)
//          )
        }

        post("/{exchangeId}/close") {
//          submitClose(
//            call = call,
//            exchangesApi = exchangesApi,
//            callback = submitCallbacks.getOrDefault("close", null)
//          )
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

  fun <T : SubmitKind> submit(messageKind: T, callback: SubmitCallback) {
    this.submitCallbacks[messageKind.toString()] = callback
  }

  fun <T : GetKind> get(resourceKind: T, callback: GetCallback) {
    this.getCallbacks[resourceKind.toString()] = callback
  }

  fun start() {
    embedded.start(wait = true)
  }

  fun stop() {
    embedded.stop(1000, 5000)
  }

}