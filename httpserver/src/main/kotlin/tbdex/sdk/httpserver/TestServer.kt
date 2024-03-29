package tbdex.sdk.httpserver

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import tbdex.sdk.httpclient.models.Exchange
import tbdex.sdk.httpserver.models.FakeExchangesApi
import tbdex.sdk.httpserver.models.FakeOfferingsApi
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Offering
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

  tbdexServer.createExchange { call, rfq, offering, replyTo ->

    println("RFQ: $rfq")
    println("Offering: $offering")
    println("ReplyTo: $replyTo")

    println("Saving RFQ...")
    println("Validating RFQ.claims against Offering...")
    println("Generating a Quote...")
    println("Saving a Quote...")
    call.respond(HttpStatusCode.Accepted)
  }

  tbdexServer.getOfferings { call ->
    println("Getting offerings...")
    call.respond(HttpStatusCode.OK, listOf<Offering>())
  }

  tbdexServer.getExchange { call ->
    println("Getting one exchange, no filter needed...")
    call.respond(HttpStatusCode.OK, listOf<Message>())
  }

  tbdexServer.getExchanges { call, filter ->
    println("Getting ALL exchanges...")
    println("Filter: $filter")
    call.respond(HttpStatusCode.OK, listOf<Exchange>())
  }

  tbdexServer.submitClose { call, close ->
    println("Submitting close...")
    println("Close: $close")
    call.respond(HttpStatusCode.Accepted)
  }

  tbdexServer.submitOrder { call, order ->
    println("Submitting order...")
    println("Order: $order")
    call.respond(HttpStatusCode.Accepted)
  }

  tbdexServer.start()

}