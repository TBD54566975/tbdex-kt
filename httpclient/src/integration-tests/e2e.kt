package tbdex

import ErrorResponse
import GetExchangeResponse
import GetOfferingsResponse
import SendMessageResponse
import TbdexResponse
import tbdex.sdk.httpclient.RealTbdexClient
import tbdex.sdk.httpclient.models.GetOfferingsFilter
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.OrderStatus
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.models.RfqData
import tbdex.sdk.protocol.models.SelectedPaymentMethod
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.DidKey

private const val pollInterval = 1000L

/**
 * An e2e test calling a tbdex client to perform a tbdex transaction.
 *
 */
fun main() {
  val client = RealTbdexClient

  @Suppress("MaximumLineLength")
  val pfiDid =
    "did:ion:EiA92VwRF0pNYrhT7TXdTO8-VSMr7bUaKJzn8ZQIEZou6g:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiIxMGUxYWRlOC05ZjIxLTQ4ZDEtOGFhZS1jYzUxZGI1ZWNkYTEiLCJwdWJsaWNLZXlKd2siOnsiYWxnIjoiRVMyNTZLIiwiY3J2Ijoic2VjcDI1NmsxIiwia2lkIjoiOVp1RzRCbkdKdmc3NWxQN2VlU3BPSDJkUDFhS1ZpZmRNaDVqd3FxUFpWQSIsImt0eSI6IkVDIiwidXNlIjoic2lnIiwieCI6ImtOWk5vUWNYYVZFUndYeUYtdmRqcFRnV2FmNTdkcHR0V1VmTURIUHgtbU0iLCJ5Ijoid21oWXRXcjBKSzc5eTNpSnBXQmFkd0tBZnktR3FRUmJzUWFUYVpDVk9COCJ9LCJwdXJwb3NlcyI6WyJhdXRoZW50aWNhdGlvbiIsImFzc2VydGlvbk1ldGhvZCJdLCJ0eXBlIjoiSnNvbldlYktleTIwMjAifV0sInNlcnZpY2VzIjpbeyJpZCI6InBmaSIsInNlcnZpY2VFbmRwb2ludCI6Imh0dHBzOi8vcGZpLnllbGxvd2NhcmQuZW5naW5lZXJpbmciLCJ0eXBlIjoiUEZJIn1dfX1dLCJ1cGRhdGVDb21taXRtZW50IjoiRWlDQzBRa0lBMzBYaDlRYjh6MnhCeW1XQWkzajlrQVNFZlpqOG9UX3dRYjI1dyJ9LCJzdWZmaXhEYXRhIjp7ImRlbHRhSGFzaCI6IkVpRHlnZDRJcGkwbU5JZkVJNVR2VWY4NTR4aE5iZXJoTjJzR1IzZE9LaFJDZGciLCJyZWNvdmVyeUNvbW1pdG1lbnQiOiJFaURsNFRYdWlwRE9lZG9LQkRzWjBjYWNidUY1T1haOG5qTmVFZlIyeExJdnJRIn19"
  val myDid = DidKey.create(InMemoryKeyManager())

  println("let's do a tbdex transaction!")
  println("Getting offerings...")
  val getOfferingsResponse = client.getOfferings(pfiDid, GetOfferingsFilter("BTC", "KES"))

  assert(
    getOfferingsResponse is GetOfferingsResponse,
    "Error getting offerings. errors: ${(getOfferingsResponse as ErrorResponse).errors}"
  )

  val offerings = (getOfferingsResponse as GetOfferingsResponse).data
  println("Got offerings! ${offerings.toString().replace("), ", "),\n")}")

  assert(
    offerings.isNotEmpty(),
    "Offering is empty"
  )

  val firstOfferingId = offerings[0].metadata.id

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
    claims = listOf("vcJwt")
  )
  val rfq = Rfq.create(pfiDid, myDid.uri, rfqData)
  rfq.sign(myDid)
  println("Sending RFQ against first offering ${offerings[0].metadata.id}")
  val sendRfqResponse = client.sendMessage(rfq)

  assert(
    sendRfqResponse is SendMessageResponse,
    "Error returned from sending RFQ. Errors: ${(sendRfqResponse as ErrorResponse).errors}"
  )

  println("Pinging for quote")

  var messages: List<Message>
  do {
    val getExchangeResponse: TbdexResponse = client.getExchange(pfiDid, rfq.metadata.exchangeId.toString(), myDid)

    assert(getExchangeResponse is GetExchangeResponse) {
      "Error returned from getting Exchanges. Errors: ${(getExchangeResponse as ErrorResponse).errors}"
    }

    messages = (getExchangeResponse as GetExchangeResponse).data
    if (messages.size < 2) {
      Thread.sleep(pollInterval)
    }
  } while (messages.size < 2)

  val quote = messages.last() as Quote
  println("Got quote! QuoteData: ${quote.data}")

  val order = Order.create(pfiDid, myDid.uri, rfq.metadata.exchangeId)
  order.sign(myDid)

  println("Sending order $order")
  val sendOrderResponse = client.sendMessage(order)

  assert(
    sendOrderResponse is SendMessageResponse,
    "Error returned from sending Order. Errors: ${(sendOrderResponse as ErrorResponse).errors}"
  )

  println("Polling for exchanges to get latest order status...")
  do {
    val getExchangeResponse: TbdexResponse = client.getExchange(pfiDid, rfq.metadata.exchangeId.toString(), myDid)

    assert(
      getExchangeResponse is GetExchangeResponse,
      "Error returned from getting Exchanges after sending Order. \n" +
        "Errors: ${(sendOrderResponse as ErrorResponse).errors}"
    )

    messages = (getExchangeResponse as GetExchangeResponse).data
    val lastMessage = messages.last()
    if (lastMessage !is OrderStatus || lastMessage.data.orderStatus !== "COMPLETED") {
      if (lastMessage is OrderStatus) {
        println("Latest orderstatus: ${lastMessage.data.orderStatus}")
      }
      Thread.sleep(pollInterval)
    }

  } while (messages.last() !is OrderStatus || (messages.last() as OrderStatus).data.orderStatus !== "COMPLETED")

  val lastMessage = messages.last() as OrderStatus

  println(
    "you have finished your mission of completing a tbdex transaction. \n" +
      "the last order message status is ${lastMessage.data.orderStatus}. \n" +
      "farewell!"
  )
}

fun assert(condition: Boolean, message: String?) {
  if (!condition) {
    throw AssertionError(message)
  }
}