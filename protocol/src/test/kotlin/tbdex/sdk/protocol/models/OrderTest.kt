package tbdex.sdk.protocol.models

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.startsWith
import de.fxlae.typeid.TypeId
import org.junit.jupiter.api.assertDoesNotThrow
import tbdex.sdk.protocol.TestData
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.Test
import kotlin.test.assertIs

class OrderTest {
  @Test
  fun `can create a new order`() {
    val order = Order.create(
      to = "pfi",
      from = "alice",
      exchangeId = TypeId.generate(MessageKind.rfq.name).toString()
    )

    assertThat(order.metadata.id).startsWith("order")
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
