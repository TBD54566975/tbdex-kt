package tbdex.sdk.httpclient

import org.junit.jupiter.api.Disabled
import tbdex.sdk.httpclient.models.ErrorResponse
import tbdex.sdk.httpclient.models.GetExchangesFilter
import tbdex.sdk.httpclient.models.GetExchangesResponse
import tbdex.sdk.httpclient.models.GetOfferingsFilter
import tbdex.sdk.httpclient.models.GetOfferingsResponse
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
    val client = RealTbdexClient

    // tbdex-mock-pfi did
    val pfiDid =
      "did:ion:EiBxWCFvogkmSmQrEpxXM3kT0bcf1_maNQi4r4FExBgSwg:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJkd24tc2lnIiwicHVibGljS2V5SndrIjp7ImNydiI6IkVkMjU1MTkiLCJrdHkiOiJPS1AiLCJ4IjoiTDlyblBQSXc3QkdEc2RBdm5PMTJuU1NjZTBZLTdLQlp0bkh6WHF4enYyTSJ9LCJwdXJwb3NlcyI6WyJhdXRoZW50aWNhdGlvbiIsImFzc2VydGlvbk1ldGhvZCJdLCJ0eXBlIjoiSnNvbldlYktleTIwMjAifV0sInNlcnZpY2VzIjpbeyJpZCI6InBmaSIsInNlcnZpY2VFbmRwb2ludCI6Imh0dHA6Ly9sb2NhbGhvc3Q6OTAwMCIsInR5cGUiOiJQRkkifV19fV0sInVwZGF0ZUNvbW1pdG1lbnQiOiJFaURhYmhUeUNRTHBHeDZJNTVnY0J1ZGlON2NJS2ZIQUtlYVBwVG5RMUVoZzB3In0sInN1ZmZpeERhdGEiOnsiZGVsdGFIYXNoIjoiRWlCQVdrOEthMlNVbzcxemZpMXo5SjBkd1hNZ0N0MlN2SjZUWTVaMmNKTzIwQSIsInJlY292ZXJ5Q29tbWl0bWVudCI6IkVpQ3pwMGlkX01SaXFQNVZ2WVlfNjM2WVZzb0xVcWY1VERFeE5jRlU4RmNBWncifX0"
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
        Thread.sleep(POLL_INTERVAL)
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
        Thread.sleep(POLL_INTERVAL)
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
