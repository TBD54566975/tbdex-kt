package tbdex.sdk.httpserver

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import tbdex.sdk.httpclient.models.Exchange
import tbdex.sdk.httpserver.models.FakeExchangesApi
import tbdex.sdk.httpserver.models.FakeOfferingsApi
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.Message
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.methods.dht.DidDht

// todo remove this before merging!!
/**
 * Main function to test the server.
 */
fun main() {
  val serverConfig = TbdexHttpServerConfig(
    port = 8080,
    pfiDid = DidDht.create(InMemoryKeyManager()).uri,
    offeringsApi = FakeOfferingsApi(),
    exchangesApi = FakeExchangesApi()
  )

  val tbdexServer = TbdexHttpServer(serverConfig)

  tbdexServer.onCreateExchange { call, rfq, offering, replyTo ->
    tbdexServer.pfiDid
    println("RFQ: $rfq")
    println("Offering: $offering")
    println("ReplyTo: $replyTo")

    println("Saving RFQ...")
    println("Validating RFQ.claims against Offering...")
    println("Generating a Quote...")
    println("Saving a Quote...")
    call.respond(HttpStatusCode.Accepted)
  }

  tbdexServer.onGetOfferings { call ->
    println("Getting offerings...")
    val offerings = tbdexServer.offeringsApi.getOfferings()
    call.respond(HttpStatusCode.OK, offerings)
  }

  tbdexServer.onGetExchange { call ->
    println("Getting one exchange, no filter needed...")
    call.respond(HttpStatusCode.OK, listOf<Message>())
  }

  tbdexServer.onGetExchanges { call, filter ->
    println("Getting ALL exchanges...")
    println("Filter: $filter")
    call.respond(HttpStatusCode.OK, listOf<Exchange>())
  }

  tbdexServer.onSubmitClose { call, close ->
    if (close is Close) {

    }
    println("Submitting close...")
    println("Close: $close")
    call.respond(HttpStatusCode.Accepted)
  }

  tbdexServer.onSubmitOrder { call, order ->
    println("Submitting order...")
    println("Order: $order")
    call.respond(HttpStatusCode.Accepted)
  }

  tbdexServer.start()

}