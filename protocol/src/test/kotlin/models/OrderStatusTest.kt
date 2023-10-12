package protocol.models

import assertk.assertThat
import assertk.assertions.isEqualTo
import models.Message
import models.MessageKind
import models.OrderStatus
import models.OrderStatusData
import protocol.TestData
import protocol.TestData.PFI_DID
import typeid.TypeID
import kotlin.test.Test
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
  fun `can parse an orderStatus from a json string`() {
    val orderStatus = TestData.getOrderStatus()
    orderStatus.sign(PFI_DID)
    val jsonMessage = orderStatus.toString()
    val parsedMessage = Message.parse(jsonMessage)

    assertIs<OrderStatus>(parsedMessage)
    assertThat(parsedMessage.toString()).isEqualTo(jsonMessage)
  }
}