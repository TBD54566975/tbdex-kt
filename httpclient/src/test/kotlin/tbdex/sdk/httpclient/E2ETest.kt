package tbdex.sdk.httpclient

import com.nimbusds.jose.jwk.JWK
import de.fxlae.typeid.TypeId
import foundation.identity.did.Service
import org.junit.jupiter.api.Disabled
import tbdex.sdk.httpclient.models.GetExchangesFilter
import tbdex.sdk.httpclient.models.GetOfferingsFilter
import tbdex.sdk.httpclient.models.TbdexResponseException
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.OrderStatus
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.models.RfqData
import tbdex.sdk.protocol.models.SelectedPaymentMethod
import web5.sdk.credentials.VerifiableCredential
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.Did
import web5.sdk.dids.PublicKeyPurpose
import web5.sdk.dids.methods.dht.CreateDidDhtOptions
import web5.sdk.dids.methods.dht.DidDht
import web5.sdk.dids.methods.key.DidKey
import java.net.URI
import kotlin.test.Test

private const val POLL_INTERVAL = 1000L

class E2ETest {
  /**
   * An e2e test calling a tbdex client to perform a tbdex transaction.
   *
   */
  @Test
  @Disabled("Must be run alongside tbdex-mock-pfi. See README for details")
  fun `tests e2e flow`() {
    val client = TbdexHttpClient

    val keyManager = InMemoryKeyManager()
    val pfiDid = DidDht.create(
      keyManager,
      CreateDidDhtOptions(
        verificationMethods = listOf(
          Pair(
            JWK.parse(
              """{
        "crv": "Ed25519",
        "kty": "OKP",
        "x": "i6cnsuH4JTBMXKbseg28Hi3w4Xp13E85UwnSW3ZgYk8"
      }"""
            ),
            arrayOf(PublicKeyPurpose.AUTHENTICATION)
          )
        ),
        services = listOf(
          Service.builder()
            .id(URI.create("#pfi"))
            .type("PFI")
            .serviceEndpoint("http://localhost:9000")
            .build()
        ),
        publish = false,
      )
    ).uri

    val myDid = DidKey.create(keyManager)

    println("let's do a tbdex transaction!")
    println("Getting offerings...")

    val offerings = try {
      client.getOfferings(pfiDid, GetOfferingsFilter("BTC", "KES"))
    } catch (e: TbdexResponseException) {
      throw AssertionError(
        "Error getting offerings. " +
          "Errors: ${e.errors?.joinToString(", ") { it.detail }}",
        e
      )
    }

    println(
      "Got offerings! payin ${offerings.first().data.payinCurrency.currencyCode}, " +
        "payout ${offerings.first().data.payoutCurrency.currencyCode}"
    )

    if (offerings.isEmpty()) {
      throw AssertionError("Offerings are empty")
    }

    val firstOfferingId = offerings[0].metadata.id

    val vc = buildVC(myDid)
    val vcJwt = vc.sign(myDid)

    val rfqData = buildRfqData(firstOfferingId, vcJwt)
    val rfq = Rfq.create(to = pfiDid, from = myDid.uri, rfqData)

    rfq.sign(myDid)

    println("Sending RFQ against first offering id: ${offerings[0].metadata.id}.")
    println("ExchangeId for the rest of this exchange is ${rfq.metadata.exchangeId}")

    try {
      client.createExchange(rfq, "https://tbdex.io/callback")
    } catch (e: TbdexResponseException) {
      throw AssertionError(
        "Error in sending RFQ. " +
          "Errors: ${e.errors?.joinToString(", ") { it.detail }}",
        e
      )
    }
    println("Pinging for quote")

    val exchangeWithQuote = getCurrentExchange(client, pfiDid, myDid, rfq)

    if (exchangeWithQuote.last() !is Quote) {
      throw AssertionError("Timed out trying to get a Quote")
    }
    val quote = exchangeWithQuote.last() as Quote
    println("Got quote! QuoteData: Hurry, quote expires at ${quote.data.expiresAt}")

    val order = Order.create(pfiDid, myDid.uri, rfq.metadata.exchangeId)
    order.sign(myDid)

    println("Sending order against Quote with exchangeId of ${order.metadata.exchangeId}")

    try {
      client.sendMessage(order)
    } catch (e: TbdexResponseException) {
      throw AssertionError(
        "Error returned from sending Order. " +
          "Errors: ${e.errors?.joinToString(", ") { it.detail }}",
        e
      )
    }

    println("Polling for exchanges to get latest order status...")
    val exchangeWithOrderStatus = getExchangeWithOrderStatus(client, pfiDid, myDid, rfq)

    val lastMessage = exchangeWithOrderStatus.last()

    if (lastMessage !is OrderStatus || lastMessage.data.orderStatus != "COMPLETED") {
      throw AssertionError("Timed out trying to get a COMPLETED OrderStatus")
    }

    println(
      "you have finished your mission of completing a tbdex transaction. \n" +
        "the last order message status is ${lastMessage.data.orderStatus}. \n" +
        "farewell!"
    )
  }

  private fun getExchangeWithOrderStatus(
    client: TbdexHttpClient,
    pfiDid: String,
    myDid: DidKey,
    rfq: Rfq,
  ): List<Message> {
    var attempt = 0
    var listOfExchanges: List<List<Message>>
    var currentExchange: List<Message>
    do {
      listOfExchanges = try {
        client.getExchanges(
          pfiDid,
          myDid,
          GetExchangesFilter(listOf(rfq.metadata.exchangeId.toString()))
        )

      } catch (e: TbdexResponseException) {
        throw AssertionError(
          "Error returned from getting Exchanges after sending Order. \n" +
            "Errors: ${e.errors?.joinToString(", ") { it.detail }}",
          e
        )
      }

      currentExchange =
        listOfExchanges.first { exchanges ->
          exchanges.any { msg ->
            msg.metadata.exchangeId == rfq.metadata.exchangeId
          }
        }
      val lastMessage = currentExchange.last()
      if (lastMessage !is OrderStatus || lastMessage.data.orderStatus !== "COMPLETED") {
        println("Attempt #$attempt at fetching exchanges after sending Order")
        if (lastMessage is OrderStatus) {
          println("Latest orderstatus: ${lastMessage.data.orderStatus}")
        }
        if (lastMessage is Order) {
          println("Still no OrderStatus, last message is Order")
        }
        Thread.sleep(POLL_INTERVAL)
        attempt++
      }
      if (attempt > 5) {
        break
      }

    } while (currentExchange.last() !is OrderStatus ||
      (currentExchange.last() as OrderStatus).data.orderStatus != "COMPLETED"
    )
    return currentExchange
  }

  private fun getCurrentExchange(
    client: TbdexHttpClient,
    pfiDid: String,
    myDid: Did,
    rfq: Rfq
  ): List<Message> {
    var listOfExchanges: List<List<Message>>
    var currentExchange: List<Message>
    var attempt = 0
    do {
      listOfExchanges = try {
        client.getExchanges(
          pfiDid,
          myDid,
          GetExchangesFilter(listOf(rfq.metadata.exchangeId.toString()))
        )
      } catch (e: TbdexResponseException) {
        throw AssertionError(
          "Error returned from getting Exchanges. " +
            "Errors: ${e.errors?.joinToString(", ") { it.detail }}",
          e
        )
      }

      if (listOfExchanges.isEmpty()) {
        throw AssertionError(
          "No Exchange available " +
            "(RFQ we sent earlier not being persisted!)"
        )
      }
      println(
        "Filtering list of exchanges for an exchange " +
          "with exchangeId of ${rfq.metadata.exchangeId}"
      )
      currentExchange =
        listOfExchanges.first { exchanges ->
          exchanges.any { msg -> msg.metadata.exchangeId == rfq.metadata.exchangeId }
        }
      if (currentExchange.size < 2) {
        Thread.sleep(POLL_INTERVAL)
        attempt++
        println("Attempt #$attempt at fetching exchanges after sending RFQ")
      }
      if (attempt > 5) {
        break
      }
    } while (currentExchange.size < 2)
    return currentExchange
  }

  private fun buildRfqData(
    firstOfferingId: TypeId,
    vcJwt: String
  ) = RfqData(
    offeringId = firstOfferingId,
    payinAmount = "1.00",
    payinMethod = SelectedPaymentMethod(
      kind = "NGN_ADDRESS",
      paymentDetails = mapOf("walletAddress" to "ngn-wallet-address")
    ),
    payoutMethod = SelectedPaymentMethod(
      kind = "BANK_Access Bank",
      paymentDetails = mapOf(
        "accountNumber" to "1234567890",
        "reason" to "gift",
        "accountHolderName" to "lainey wilson"
      )
    ),
    claims = listOf(vcJwt)
  )

  private fun buildVC(myDid: DidKey) = VerifiableCredential.create(
    type = "SanctionCredential",
    issuer = myDid.uri,
    subject = myDid.uri,
    data = mapOf("fullName" to "Lainey Wilson", "country" to "US")
  )
}
