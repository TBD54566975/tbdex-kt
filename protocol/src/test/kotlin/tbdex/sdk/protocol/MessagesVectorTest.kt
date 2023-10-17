package tbdex.sdk.protocol

import org.junit.jupiter.api.Test
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.OrderStatus
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.Rfq
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MessagesVectorTest {
  @Test
  fun `parse rfq`() {
    testParsing<Rfq>(TestVectors.rfq())
  }

  @Test
  fun `serialise rfq`() {
    testSerialisation(TestVectors.rfq())
  }
  @Test
  fun `parse quote`() {
    testParsing<Quote>(TestVectors.quote())
  }

  @Test
  fun `serialise quote`() {
    testSerialisation(TestVectors.quote())
  }
  @Test
  fun `parse order`() {
    testParsing<Order>(TestVectors.order())
  }

  @Test
  fun `serialise order`() {
    testSerialisation(TestVectors.order())
  }
  @Test
  fun `parse order status`() {
    testParsing<OrderStatus>(TestVectors.orderStatus())
  }

  @Test
  fun `serialise order status`() {
    testSerialisation(TestVectors.orderStatus())
  }
  @Test
  fun `parse close`() {
    testParsing<Close>(TestVectors.close())
  }

  @Test
  fun `serialise close`() {
    testSerialisation(TestVectors.close())
  }

  /**
   * Test parse, validate, and verify on the [vectorString].
   */
  private inline fun <reified T> testParsing(vectorString: String) {
    val tbDEXMessage = Message.parse(vectorString)
    assertIs<T>(tbDEXMessage)
  }

  private fun testSerialisation(original: String) {
    val tbDEXMessage = Message.parse(original)
    val serialised = Json.stringify(tbDEXMessage)

    assertEquals(original, serialised)
  }

}