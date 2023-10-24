package tbdex.sdk.httpclient

import com.nimbusds.jose.JWSAlgorithm
import tbdex.sdk.httpclient.models.ErrorResponse
import tbdex.sdk.httpclient.models.GetExchangeResponse
import tbdex.sdk.httpclient.models.GetOfferingsFilter
import tbdex.sdk.httpclient.models.GetOfferingsResponse
import tbdex.sdk.httpclient.models.TbdexResponse
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.OrderStatus
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.models.RfqData
import tbdex.sdk.protocol.models.SelectedPaymentMethod
import web5.sdk.credentials.VerifiableCredential
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
  // generated-did
//  val pfiDid =
//    "did:ion:EiA92VwRF0pNYrhT7TXdTO8-VSMr7bUaKJzn8ZQIEZou6g:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiIxMGUxYWRlOC05ZjIxLTQ4ZDEtOGFhZS1jYzUxZGI1ZWNkYTEiLCJwdWJsaWNLZXlKd2siOnsiYWxnIjoiRVMyNTZLIiwiY3J2Ijoic2VjcDI1NmsxIiwia2lkIjoiOVp1RzRCbkdKdmc3NWxQN2VlU3BPSDJkUDFhS1ZpZmRNaDVqd3FxUFpWQSIsImt0eSI6IkVDIiwidXNlIjoic2lnIiwieCI6ImtOWk5vUWNYYVZFUndYeUYtdmRqcFRnV2FmNTdkcHR0V1VmTURIUHgtbU0iLCJ5Ijoid21oWXRXcjBKSzc5eTNpSnBXQmFkd0tBZnktR3FRUmJzUWFUYVpDVk9COCJ9LCJwdXJwb3NlcyI6WyJhdXRoZW50aWNhdGlvbiIsImFzc2VydGlvbk1ldGhvZCJdLCJ0eXBlIjoiSnNvbldlYktleTIwMjAifV0sInNlcnZpY2VzIjpbeyJpZCI6InBmaSIsInNlcnZpY2VFbmRwb2ludCI6Imh0dHBzOi8vcGZpLnllbGxvd2NhcmQuZW5naW5lZXJpbmciLCJ0eXBlIjoiUEZJIn1dfX1dLCJ1cGRhdGVDb21taXRtZW50IjoiRWlDQzBRa0lBMzBYaDlRYjh6MnhCeW1XQWkzajlrQVNFZlpqOG9UX3dRYjI1dyJ9LCJzdWZmaXhEYXRhIjp7ImRlbHRhSGFzaCI6IkVpRHlnZDRJcGkwbU5JZkVJNVR2VWY4NTR4aE5iZXJoTjJzR1IzZE9LaFJDZGciLCJyZWNvdmVyeUNvbW1pdG1lbnQiOiJFaURsNFRYdWlwRE9lZG9LQkRzWjBjYWNidUY1T1haOG5qTmVFZlIyeExJdnJRIn19"
  // tbdex-mock-pfi did
  val pfiDid =
    "did:ion:EiBwNQC_lRp1cAOZJC3XmtCXzSIL_rr0JOVYG82ORwVE_g:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJkd24tc2lnIiwicHVibGljS2V5SndrIjp7ImNydiI6IkVkMjU1MTkiLCJrdHkiOiJPS1AiLCJ4IjoiaTZjbnN1SDRKVEJNWEtic2VnMjhIaTN3NFhwMTNFODVVd25TVzNaZ1lrOCJ9LCJwdXJwb3NlcyI6WyJhdXRoZW50aWNhdGlvbiJdLCJ0eXBlIjoiSnNvbldlYktleTIwMjAifV0sInNlcnZpY2VzIjpbeyJpZCI6InBmaSIsInNlcnZpY2VFbmRwb2ludCI6Imh0dHA6Ly9sb2NhbGhvc3Q6OTAwMCIsInR5cGUiOiJQRkkifV19fV0sInVwZGF0ZUNvbW1pdG1lbnQiOiJFaUNSNlB0MGY3SkRORVdqaFlsazBOdWtJSVFOMGVyc3ZfdHJLeERKdTlmZHZRIn0sInN1ZmZpeERhdGEiOnsiZGVsdGFIYXNoIjoiRWlBLTEzYUNoMmRrcW9oOWIxWDZudnppSXI2VS1JYUpJTFd3UU5sZjJNczBkZyIsInJlY292ZXJ5Q29tbWl0bWVudCI6IkVpQlhoX2dsN1pCd2JUOGNHOU5LT0FSV0xSUzZWUjRRVm44OWEyWldtdzhHOXcifX0"
  val issuerDid = DidKey("did:key:z6MkhVUvUEdEbFpQgRyqLryBfE3frxuEyNsqWUmhm2FoKhpp", InMemoryKeyManager())
  val keyAlias = issuerDid.keyManager.generatePrivateKey(JWSAlgorithm.ES256K)
  println("keyalias $keyAlias")
  val pubKey = issuerDid.keyManager.getPublicKey(keyAlias)
  println("pubkey $pubKey")
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
  println("Got offerings! ${offerings.toString().replace("), ", "),\n")}")

  if (offerings.isEmpty()) {
    throw AssertionError("Offerings are empty")
  }

  val firstOfferingId = offerings[0].metadata.id
  val vc = VerifiableCredential.create(
    type = "SanctionCredential",
    issuer = issuerDid.uri,
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
  println("Sending RFQ against first offering ${offerings[0].metadata.id}")
  println("Claim ${rfq.data.claims[0]}")
  val sendRfqResponse = client.sendMessage(rfq)

  if (sendRfqResponse is ErrorResponse) {
    throw AssertionError(
      "Error returned from sending RFQ. " +
        "Errors: ${sendRfqResponse.errors?.joinToString(", ") { it.detail }}"
    )
  }

  println("Pinging for quote")

  var messages: List<Message>
  do {
    val getExchangeResponse: TbdexResponse = client.getExchange(pfiDid, rfq.metadata.exchangeId.toString(), myDid)

    if (getExchangeResponse is ErrorResponse) {
      throw AssertionError(
        "Error returned from getting Exchanges. " +
          "Errors: ${getExchangeResponse.errors?.joinToString(", ") { it.detail }}"
      )
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

  if (sendOrderResponse is ErrorResponse) {
    throw AssertionError(
      "Error returned from sending Order. " +
        "Errors: ${sendOrderResponse.errors?.joinToString(", ") { it.detail }}"
    )
  }

  println("Polling for exchanges to get latest order status...")
  do {
    val getExchangeResponse: TbdexResponse = client.getExchange(pfiDid, rfq.metadata.exchangeId.toString(), myDid)

    if (getExchangeResponse is ErrorResponse) {
      throw AssertionError("Error returned from getting Exchanges after sending Order. \n" +
        "Errors: ${getExchangeResponse.errors?.joinToString(", ") { it.detail }}"
      )
    }

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