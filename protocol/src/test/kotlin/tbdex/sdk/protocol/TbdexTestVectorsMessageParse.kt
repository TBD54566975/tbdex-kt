package tbdex.sdk.protocol

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.OrderStatus
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TbdexTestVectorsMessageParse {
  @Test
  fun `parse-close json`() {
    val vector = TestVectors.getVector("parse-close.json")
    assertNotNull(vector)
    testNonErrorTestVector<Close>(vector)
  }

  @Test
  fun `parse-order json`() {
    val vector = TestVectors.getVector("parse-order.json")
    assertNotNull(vector)
    testNonErrorTestVector<Order>(vector)
  }

  @Test
  fun `parse-orderstatus json`() {
    val vector = TestVectors.getVector("parse-orderstatus.json")
    assertNotNull(vector)
    testNonErrorTestVector<OrderStatus>(vector)
  }


  @Test
  fun `parse-quote json`() {
    val vector = TestVectors.getVector("parse-quote.json")
    assertNotNull(vector)
    testNonErrorTestVector<Quote>(vector)
  }

  @Test
  fun `parse-rfq json`() {
    val vector = TestVectors.getVector("parse-rfq.json")
    assertNotNull(vector)
    testNonErrorTestVector<Rfq>(vector)
  }

  private inline fun <reified T> testNonErrorTestVector(vector: JsonNode) {
    val input = vector["input"].textValue()
    assertNotNull(input)

    val tbDEXMessage = Message.parse(input)
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