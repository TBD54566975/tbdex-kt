package protocol.models

import assertk.assertThat
import assertk.assertions.isEqualTo
import models.Message
import models.MessageKind
import models.OrderStatus
import models.OrderStatusData
import protocol.TestData
import typeid.TypeID
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertIs

class OrderStatusTest {
  @Test
  fun `can create a new orderStatus`() {
    val orderStatus = OrderStatus.create(
      "pfi", "alice", TypeID(MessageKind.rfq.name), OrderStatusData("my status")
    )

    assertk.assertAll {
      assertThat(orderStatus.metadata.id.prefix).isEqualTo("orderstatus")
      assertThat(orderStatus.data.status).isEqualTo("my status")
    }
  }

  @Test
  fun `sign populates orderStatus signature`() {
    val orderStatus = TestData.getOrderStatus()
    orderStatus.sign("fakepk", "fakekid")

    assertThat(orderStatus.signature).isEqualTo("blah")
  }

  @Test
  fun `can parse an orderStatus from a json string`() {
    val orderStatus = TestData.getOrderStatus()
    orderStatus.sign("fakepk", "fakekid")
    val jsonMessage = orderStatus.toJson()
    val parsedMessage = Message.parse(jsonMessage)

    assertIs<OrderStatus>(parsedMessage)
    assertThat(parsedMessage.toJson()).isEqualTo(jsonMessage)
  }

  @Test
  fun `can validate an orderStatus`() {
    val orderStatus = TestData.getOrderStatus()
    orderStatus.sign("fakepk", "fakekid")

    try {
      Message.validate(Json.stringify(orderStatus))
    } catch (e: Exception) {
      throw e
    }
  }
}