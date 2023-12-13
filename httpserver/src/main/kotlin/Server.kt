package tbdex.server

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import tbdex.server.tbdex.server.requestHandlers.submitRfq


class TbdexHttpServer(private val offeringsApi: OfferingsApi? = null, private val exchangesApi: ExchangesApi? = null) {

  private val getCallbacks: MutableMap<String, GetCallback<*>> = mutableMapOf()
  private val submitCallbacks: MutableMap<String, SubmitCallback<*, *>> = mutableMapOf()

  fun <T> get(resourceKind: String, callback: GetCallback<T>) {
    getCallbacks[resourceKind] = callback
  }

  fun <T, O> submit(messageKind: String, callback: SubmitCallback<T, O>) {
    submitCallbacks[messageKind] = callback
  }

  /**
   * Main entrypoint of the executable that starts a Netty webserver at port 8080
   * and registers the [module].
   * e.g.
   *  val server = TbdexHttpServer(offeringsApi, exchangesApi)
   *  server.listen()
   */
  fun listen() {
    embeddedServer(Netty, port = 8080) { module() }.start(wait = true)
  }

  private fun Application.module() {
    routing {
      get("/") {
        call.respondText {
          "Please use the tbdex protocol to communicate with this server " +
            "or a suitable library: https://github.com/TBD54566975/tbdex-protocol"
        }
      }

      post("/exchanges/{exchangeId}/rfq") {

        this.call.parameters["exchangeId"]?.let { exchangeId ->

          val message = call.receiveText()
          val submitCallback = submitCallbacks["rfq"]
          submitRfq(exchangeId, message, submitCallback)
        }

      }

    }
  }

}