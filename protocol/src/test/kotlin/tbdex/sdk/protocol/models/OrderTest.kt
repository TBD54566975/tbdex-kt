package tbdex.sdk.protocol.models

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.assertDoesNotThrow
import tbdex.sdk.protocol.TestData
import tbdex.sdk.protocol.Json
import typeid.TypeID
import kotlin.test.Test
import kotlin.test.assertIs

class OrderTest {
  @Test
  fun `can create a new order`() {
    val order = Order.create("pfi", "alice", TypeID(MessageKind.rfq.name))

    assertThat(order.metadata.id.prefix).isEqualTo("order")
  }

  @Test
  fun `can parse an order from a json string`() {
    val order = TestData.getOrder()
    order.sign(TestData.ALICE_DID)
    val jsonMessage = order.toString()
    val parsedMessage = Message.parse(jsonMessage)

    assertIs<Order>(parsedMessage)
    assertThat(parsedMessage.toString()).isEqualTo(jsonMessage)
  }

  @Test
  fun `can validate an order`() {
    val order = TestData.getOrder()
    order.sign(TestData.ALICE_DID)

    assertDoesNotThrow { Message.parse(Json.stringify(order)) }
  }
}
