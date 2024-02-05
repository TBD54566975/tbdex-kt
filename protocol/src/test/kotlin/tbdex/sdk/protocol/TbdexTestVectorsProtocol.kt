package tbdex.sdk.protocol

import com.fasterxml.jackson.databind.JsonNode
import org.junit.jupiter.api.Test
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.OrderStatus
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.Resource
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TbdexTestVectorsProtocol {
  /**
   * Tbdex Test Vectors Message Tests
   */
  @Test
  fun parse_close() {
    val vector = TestVectors.getVector("parse-close.json")
    assertNotNull(vector)
    testNonErrorMessageTestVector<Close>(vector)
  }

  @Test
  fun parse_order() {
    val vector = TestVectors.getVector("parse-order.json")
    assertNotNull(vector)
    testNonErrorMessageTestVector<Order>(vector)
  }

  @Test
  fun parse_orderstatus() {
    val vector = TestVectors.getVector("parse-orderstatus.json")
    assertNotNull(vector)
    testNonErrorMessageTestVector<OrderStatus>(vector)
  }


  @Test
  fun parse_quote() {
    val vector = TestVectors.getVector("parse-quote.json")
    assertNotNull(vector)
    testNonErrorMessageTestVector<Quote>(vector)
  }

  @Test
  fun parse_rfq() {
    val vector = TestVectors.getVector("parse-rfq.json")
    assertNotNull(vector)
    testNonErrorMessageTestVector<Rfq>(vector)
  }

  /**
   * Tbdex Test Vectors Resource Tests
   */
  @Test
  fun parse_offering() {
    val vector = TestVectors.getVector("parse-offering.json")
    assertNotNull(vector)
    testNonErrorResourceTestVector<Offering>(vector)
  }

  private inline fun <reified T> testNonErrorMessageTestVector(vector: JsonNode) {
    val input = vector["input"].textValue()
    assertNotNull(input)

    val tbDEXMessage = Message.parse(input)
    assertIs<T>(tbDEXMessage)

    assertEquals(vector["output"], Json.jsonMapper.readTree(tbDEXMessage.toString()))
  }

  private inline fun <reified T> testNonErrorResourceTestVector(vector: JsonNode) {
    val input = vector["input"].textValue()
    assertNotNull(input)

    val tbDEXMessage = Resource.parse(input)
    assertIs<T>(tbDEXMessage)

    assertEquals(vector["output"], Json.jsonMapper.readTree(tbDEXMessage.toString()))
  }

  // When we create test vectors with `error: true`
  // private fun testErrorTestVector(vector: JsonNode) {
  //   val input = vector["input"].textValue()
  //   assertNotNull(input)
  //   assertThrows(Message.parse(vector["input"])
  // }
}