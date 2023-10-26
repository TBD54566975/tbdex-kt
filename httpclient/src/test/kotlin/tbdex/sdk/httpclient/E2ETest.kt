package tbdex.sdk.httpclient

import tbdex.sdk.httpclient.models.ErrorResponse
import tbdex.sdk.httpclient.models.GetExchangesFilter
import tbdex.sdk.httpclient.models.GetExchangesResponse
import tbdex.sdk.httpclient.models.GetOfferingsFilter
import tbdex.sdk.httpclient.models.GetOfferingsResponse
import tbdex.sdk.httpclient.models.SendMessageResponse
import tbdex.sdk.httpclient.models.TbdexResponse
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.OrderStatus
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.models.RfqData
import tbdex.sdk.protocol.models.SelectedPaymentMethod
import web5.sdk.credentials.VerifiableCredential
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.DidKey
import kotlin.test.Ignore
import kotlin.test.Test

private const val pollInterval = 1000L

class E2ETest {
  /**
   * An e2e test calling a tbdex client to perform a tbdex transaction.
   *
   */
  @Test
  @Ignore
  fun `tests e2e flow`() {
    val client = RealTbdexClient

    // tbdex-mock-pfi did
    val pfiDid =
      "did:ion:EiAaZYIgSSwrAqds9V5HSVshYmz1g5EUxV0cqlAFgBM4ZA:" +
        "eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwi" +
        "ZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJkd24tc2lnIiwi" +
        "cHVibGljS2V5SndrIjp7ImNydiI6IkVkMjU1MTkiLCJrdHkiOiJPS1AiL" +
        "CJ4IjoieEVnVkNvVEYtUHpXejQ2elFuUEVQS2RtTk9mTWpuVXVhWWx0Qll" +
        "UZGxtWSJ9LCJwdXJwb3NlcyI6WyJhdXRoZW50aWNhdGlvbiIsImFzc2Vyd" +
        "Glvbk1ldGhvZCJdLCJ0eXBlIjoiSnNvbldlYktleTIwMjAifV0sInNlcnZ" +
        "pY2VzIjpbeyJpZCI6InBmaSIsInNlcnZpY2VFbmRwb2ludCI6Imh0dHA6L" +
        "y9sb2NhbGhvc3Q6OTAwMCIsInR5cGUiOiJQRkkifV19fV0sInVwZGF0ZUNv" +
        "bW1pdG1lbnQiOiJFaUR0YnphSndFaG5kbS05Y1Q0eDc2OGFTUmIxS29nRVh" +
        "0MzJEbkZ5OU05Q2xnIn0sInN1ZmZpeERhdGEiOnsiZGVsdGFIYXNoIjoiRWl" +
        "Ed3l2OXhsQW83cUt4NjRjOThHeTJKMFliTldwbGw2ZkNfcmJ5eGZ3b29pQSIs" +
        "InJlY292ZXJ5Q29tbWl0bWVudCI6IkVpQ1hMaWVpRnlCa1hKYU84eEVwOTV0Z" +
        "Ed3bUlaVG4wZEV4UnpBR3MyaXJueHcifX0"
    val myDid = DidKey.create(InMemoryKeyManager())

    println("let's do a tbdex transaction!")
    println("Getting offerings...")
    val getOfferingsResponse = client.getOfferings(pfiDid, GetOfferingsFilter("BTC", "KES"))

    if (getOfferingsResponse is ErrorResponse) {
      throw AssertionError(
        "Error getting offerings. " +
          "Errors: ${getOfferingsResponse.errors?.joinToString(", ") { it.detail }}"
      )
    }

    val offerings = (getOfferingsResponse as GetOfferingsResponse).data
    println(
      "Got offerings! payin ${(offerings.first() as Offering).data.payinCurrency.currencyCode}, " +
        "payout ${(offerings.first() as Offering).data.payoutCurrency.currencyCode}"
    )

    if (offerings.isEmpty()) {
      throw AssertionError("Offerings are empty")
    }

    val firstOfferingId = offerings[0].metadata.id
    val vc = VerifiableCredential.create(
      type = "SanctionCredential",
      issuer = myDid.uri,
      subject = myDid.uri,
      data = mapOf("fullName" to "Lainey Wilson", "country" to "US")
    )

    val vcJwt = vc.sign(myDid)
    val rfqData = RfqData(
      offeringId = firstOfferingId,
      payinSubunits = "100",
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
    val rfq = Rfq.create(pfiDid, myDid.uri, rfqData)
    rfq.sign(myDid)
    println("Sending RFQ against first offering id: ${offerings[0].metadata.id}")
    val sendRfqResponse = client.sendMessage(rfq)

    if (sendRfqResponse is ErrorResponse) {
      throw AssertionError(
        "Error returned from sending RFQ. " +
          "Errors: ${sendRfqResponse.errors?.joinToString(", ") { it.detail }}"
      )
    }
    println("SendRfqResponse is success?? ${sendRfqResponse is SendMessageResponse}")
    println("Pinging for quote")

    var listOfExchanges: List<List<Message>>
    var currentExchange: List<Message>
    var attempt = 0
    do {
      val getExchangeResponse: TbdexResponse = client.getExchanges(
        pfiDid,
        myDid,
        GetExchangesFilter(listOf(rfq.metadata.exchangeId.toString()))
      )

      if (getExchangeResponse is ErrorResponse) {
        throw AssertionError(
          "Error returned from getting Exchanges. " +
            "Errors: ${getExchangeResponse.errors?.joinToString(", ") { it.detail }}"
        )
      }

      listOfExchanges = (getExchangeResponse as GetExchangesResponse).data
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
        Thread.sleep(pollInterval)
        attempt++
        println("Attempt #$attempt at fetching exchanges after sending RFQ")
      }
      if (attempt > 5) {
        break
      }
    } while (currentExchange.size < 2)

    if (currentExchange.last() !is Quote) {
      throw AssertionError("Timed out trying to get a Quote")
    }
    val quote = currentExchange.last() as Quote
    println("Got quote! QuoteData: Hurry, quote expires at ${quote.data.expiresAt}")

    val order = Order.create(pfiDid, myDid.uri, rfq.metadata.exchangeId)
    order.sign(myDid)

    println("Sending order against Quote with exchangeId of ${order.metadata.exchangeId}")
    val sendOrderResponse = client.sendMessage(order)

    if (sendOrderResponse is ErrorResponse) {
      throw AssertionError(
        "Error returned from sending Order. " +
          "Errors: ${sendOrderResponse.errors?.joinToString(", ") { it.detail }}"
      )
    }

    println("Successfully sent order?? ${sendOrderResponse is SendMessageResponse}")
    println("Polling for exchanges to get latest order status...")
    attempt = 0
    do {
      val getExchangeResponse: TbdexResponse =
        client.getExchanges(
          pfiDid,
          myDid,
          GetExchangesFilter(listOf(rfq.metadata.exchangeId.toString()))
        )

      if (getExchangeResponse is ErrorResponse) {
        throw AssertionError(
          "Error returned from getting Exchanges after sending Order. \n" +
            "Errors: ${getExchangeResponse.errors?.joinToString(", ") { it.detail }}"
        )
      }

      listOfExchanges = (getExchangeResponse as GetExchangesResponse).data
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
        Thread.sleep(pollInterval)
        attempt++
      }
      if (attempt > 5) {
        break
      }

    } while (currentExchange.last() !is OrderStatus ||
      (currentExchange.last() as OrderStatus).data.orderStatus != "COMPLETED"
    )

    val lastMessage = currentExchange.last()

    if (lastMessage !is OrderStatus || lastMessage.data.orderStatus != "COMPLETED") {
      throw AssertionError("Timed out trying to get a COMPLETED OrderStatus")
    }

    println(
      "you have finished your mission of completing a tbdex transaction. \n" +
        "the last order message status is ${lastMessage.data.orderStatus}. \n" +
        "farewell!"
    )
  }
}
