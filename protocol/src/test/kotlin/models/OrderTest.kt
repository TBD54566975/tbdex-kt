package protocol.models

import assertk.assertThat
import assertk.assertions.isEqualTo
import models.Message
import models.MessageKind
import models.Order
import org.junit.jupiter.api.assertDoesNotThrow
import protocol.TestData
import typeid.TypeID
import kotlin.test.Test
import kotlin.test.assertContains
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
    order.sign("fakepk", "fakekid")

    assertDoesNotThrow { Message.parse(Json.stringify(order)) }
  }
}
