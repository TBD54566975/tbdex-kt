package tbdex.sdk.protocol

import com.fasterxml.jackson.databind.JsonNode
import org.junit.jupiter.api.Test
import tbdex.sdk.protocol.models.Balance
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
    testSuccessMessageTestVector<Close>(vector)
  }

  @Test
  fun parse_order() {
    val vector = TestVectors.getVector("parse-order.json")
    assertNotNull(vector)
    testSuccessMessageTestVector<Order>(vector)
  }

  @Test
  fun parse_orderstatus() {
    val vector = TestVectors.getVector("parse-orderstatus.json")
    assertNotNull(vector)
    testSuccessMessageTestVector<OrderStatus>(vector)
  }


  @Test
  fun parse_quote() {
    val vector = TestVectors.getVector("parse-quote.json")
    assertNotNull(vector)
    testSuccessMessageTestVector<Quote>(vector)
  }

  @Test
  fun parse_rfq() {
    val vector = TestVectors.getVector("parse-rfq.json")
    assertNotNull(vector)
    testSuccessMessageTestVector<Rfq>(vector)
  }

  @Test
  fun parse_rfq_omit_private_data() {
    val vector = TestVectors.getVector("parse-rfq-omit-private-data.json")
    assertNotNull(vector)
    testSuccessMessageTestVector<Rfq>(vector)
  }

  /**
   * Tbdex Test Vectors Resource Tests
   */
  @Test
  fun parse_offering() {
    val vector = TestVectors.getVector("parse-offering.json")
    assertNotNull(vector)
    testSuccessResourceTestVector<Offering>(vector)
  }

  @Test
  fun parse_balance() {
    val vector = TestVectors.getVector("parse-balance.json")
    assertNotNull(vector)
    testSuccessResourceTestVector<Balance>(vector)
  }

  private inline fun <reified T> testSuccessMessageTestVector(vector: JsonNode) {
    val input = vector["input"].textValue()
    assertNotNull(input)

    val tbDEXMessage = Parser.parseMessage(input)
    assertIs<T>(tbDEXMessage)

    assertEquals(vector["output"], Json.jsonMapper.readTree(tbDEXMessage.toString()))
  }

  private inline fun <reified T> testSuccessResourceTestVector(vector: JsonNode) {
    val input = vector["input"].textValue()
    assertNotNull(input)

    val tbDEXMessage = Parser.parseResource(input)
    assertIs<T>(tbDEXMessage)

    assertEquals(vector["output"], Json.jsonMapper.readTree(tbDEXMessage.toString()))
  }

  // When we create test vectors with `error: true`
  // private fun testErrorTestVector(vector: JsonNode) {
  //   val input = vector["input"].textValue()
  //   assertNotNull(input)
  //   assertThrows(Parser.parseMessage(vector["input"])
  // }
}