package tbdex.sdk.protocol.vectors

import tbdex.sdk.protocol.TestData
import tbdex.sdk.protocol.serialization.Json

fun main() {
  TestVectorBuilder.writeTestVectors()
}

/**
 * Utility to produce the tbDEX test vectors JSON file.
 */
object TestVectorBuilder {
  fun writeTestVectors() {
    val vectors = buildTestVectors()
    val json = Json.stringify(vectors)
    println(json)
    //write to test/resources/testVectors.json
  }

  fun buildTestVectors(): Map<String, Map<String, Any>> {
    return mapOf(
      "resources" to mapOf(
        "offering" to offering()
      ),
      "messages" to mapOf(
        "rfq" to rfq(),
        "quote" to quote(),
        "order" to order(),
        "orderStatus" to orderStatus(),
        "close" to close()
      )
    )
  }

  fun offering(): Any {
    val offering = TestData.getOffering()
    offering.sign(TestData.PFI_DID)
    return offering
  }

  fun rfq(): Any {
    val rfq = TestData.getRfq(claims = listOf("presentation submission"))
    rfq.sign(TestData.ALICE_DID)
    return rfq
  }

  fun quote(): Any {
    val quote = TestData.getQuote()
    quote.sign(TestData.PFI_DID)
    return quote
  }

  fun order(): Any {
    val order = TestData.getOrder()
    order.sign(TestData.ALICE_DID)
    return order
  }

  fun orderStatus(): Any {
    val orderStatus = TestData.getOrderStatus()
    orderStatus.sign(TestData.PFI_DID)
    return orderStatus
  }

  fun close(): Any {
    val close = TestData.getClose()
    close.sign(TestData.PFI_DID)
    return close
  }
}