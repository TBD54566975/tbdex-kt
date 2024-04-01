package tbdex.sdk.protocol.models

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.startsWith
import de.fxlae.typeid.TypeId
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import tbdex.sdk.protocol.Parser
import tbdex.sdk.protocol.TestData
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.Test
import kotlin.test.assertIs

class OrderStatusTest {
  @Test
  fun `can create a new orderStatus`() {
    val orderStatus = OrderStatus.create(
      to = "pfi",
      from = "alice",
      exchangeId = TypeId.generate(MessageKind.rfq.name).toString(),
      orderStatusData = OrderStatusData("my status")
    )

    assertAll {
      assertThat(orderStatus.metadata.id).startsWith("orderstatus")
      assertThat(orderStatus.metadata.protocol).isEqualTo("1.0")
      assertThat(orderStatus.data.orderStatus).isEqualTo("my status")
    }
  }

  @Test
  fun `can parse an orderStatus from a json string`() {
    val orderStatus = TestData.getOrderStatus()
    orderStatus.sign(TestData.PFI_DID)
    val jsonMessage = orderStatus.toString()
    val parsedMessage = OrderStatus.parse(jsonMessage)

    assertIs<OrderStatus>(parsedMessage)
    assertThat(parsedMessage.toString()).isEqualTo(jsonMessage)
  }

  @Test
  fun `parse() throws if json string is not an OrderStatus`() {
    val quote = TestData.getQuote()
    quote.sign(TestData.ALICE_DID)
    val jsonMessage = quote.toString()
    assertThrows<IllegalArgumentException> { OrderStatus.parse(jsonMessage) }
  }

  @Test
  fun `can validate an orderStatus`() {
    val orderStatus = TestData.getOrderStatus()
    orderStatus.sign(TestData.PFI_DID)

    assertDoesNotThrow { OrderStatus.parse(Json.stringify(orderStatus)) }
  }
}
