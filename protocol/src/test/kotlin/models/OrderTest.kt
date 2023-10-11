package protocol.models

import assertk.assertThat
import assertk.assertions.isEqualTo
import models.Message
import models.MessageKind
import models.Order
import protocol.TestData
import typeid.TypeID
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertIs

class OrderTest {
  @Test
  fun `can create a new order`() {
    val order = Order.create("pfi", "alice", TypeID(MessageKind.rfq.name))

    assertContains(order.metadata.id, "order")
  }

  @Test
  fun `sign populates order signature`() {
    val order = TestData.getOrder()
    order.sign("fakepk", "fakekid")

    assertThat(order.signature).isEqualTo("blah")
  }

  @Test
  fun `can parse an order from a json string`() {
    val order = TestData.getOrder()
    order.sign("fakepk", "fakekid")
    val jsonMessage = order.toJson()
    val parsedMessage = Message.parse(jsonMessage)

    assertIs<Order>(parsedMessage)
    assertThat(parsedMessage.toJson()).isEqualTo(jsonMessage)
  }

  @Test
  fun `can validate an order`() {
    val order = TestData.getOrder()
    order.sign("fakepk", "fakekid")

    try {
      Message.validate(Json.stringify(order))
    } catch (e: Exception) {
      throw e
    }
  }
}