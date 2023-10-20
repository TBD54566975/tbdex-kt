package tbdex

import ErrorResponse
import GetExchangeResponse
import GetOfferingsResponse
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
  val pfiDid = "did:pfi:0:0" // todo use a real did
  val myDid = DidKey.create(InMemoryKeyManager())

  println("let's do a tbdex transaction!")
  println("Getting offerings...")
  val getOfferingsResponse = client.getOfferings(pfiDid, GetOfferingsFilter("BTC", "KES"))

  assert(getOfferingsResponse !is ErrorResponse) { "Error getting offerings. errors: ${(getOfferingsResponse as ErrorResponse).errors}" }

  val offerings = (getOfferingsResponse as GetOfferingsResponse).data
  println("Got offerings! ${offerings.toString().replace("), ", "),\n")}")

  assert(offerings.isNotEmpty())

  val firstOfferingId = offerings[0].metadata.id

  val rfqData = RfqData(
    offeringId = firstOfferingId,
    payinSubunits = "100",
    payinMethod = SelectedPaymentMethod(kind = "BALANCE"),
    payoutMethod = SelectedPaymentMethod(kind = "MOMO_MPESA", paymentDetails = mapOf("name" to "lainey wilson", "phoneNumber" to "1234567890")),
    claims = listOf("vcJwt")
    )
  val rfq = Rfq.create(pfiDid, myDid.uri, rfqData)

  println("Sending RFQ against first offering ${offerings[0].metadata.id}")
  val sendRfqResponse = client.sendMessage(rfq)

  assert(sendRfqResponse !is ErrorResponse) { "Error returned from sending RFQ. Errors: ${(sendRfqResponse as ErrorResponse).errors}"}

  println("Pinging for quote")

  var messages: List<Message>
  do {
    val getExchangeResponse: TbdexResponse = client.getExchange(pfiDid, rfq.metadata.exchangeId.toString(), myDid)

    assert(getExchangeResponse !is ErrorResponse) { "Error returned from getting Exchanges. Errors: ${(getExchangeResponse as ErrorResponse).errors}" }

    messages = (getExchangeResponse as GetExchangeResponse).data
    if (messages.size < 2) {
      Thread.sleep(pollInterval)
    }
  } while (messages.size < 2)

  val quote = messages.last() as Quote
  println("Got quote! QuoteData: ${quote.data}")

  val order = Order.create(pfiDid, myDid.uri, rfq.metadata.exchangeId)
  println("Sending order $order")
  val sendOrderResponse = client.sendMessage(order)
  assert(sendOrderResponse !is ErrorResponse) { "Error returned from sending Order. Errors: ${(sendOrderResponse as ErrorResponse).errors}" }

  println("Polling for exchanges to get latest order status...")
  do {
    val getExchangeResponse: TbdexResponse = client.getExchange(pfiDid, rfq.metadata.exchangeId.toString(), myDid)

    assert(getExchangeResponse !is ErrorResponse) { "Error returned from getting Exchanges after sending Order. Errors: ${(sendOrderResponse as ErrorResponse).errors}" }

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
  assert(lastMessage.data.orderStatus == "COMPLETED")
  println("you have finished your mission of completing a tbdex transaction. the last order message status is COMPLETED. farewell!")
}