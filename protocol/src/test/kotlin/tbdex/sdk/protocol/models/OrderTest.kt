package tbdex.sdk.protocol.models

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.startsWith
import de.fxlae.typeid.TypeId
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import tbdex.sdk.protocol.Parser
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

    assertAll{
      assertThat(order.metadata.id).startsWith("order")
      assertThat(order.metadata.protocol).isEqualTo("1.0")
    }
  }

  @Test
  fun `can parse an order from a json string`() {
    val order = TestData.getOrder()
    order.sign(TestData.ALICE_DID)
    val jsonMessage = order.toString()
    val parsedMessage = Order.parse(jsonMessage)

    assertIs<Order>(parsedMessage)
    assertThat(parsedMessage.toString()).isEqualTo(jsonMessage)
  }

  @Test
  fun `parse() throws if json string is not an Order`() {
    val quote = TestData.getQuote()
    quote.sign(TestData.ALICE_DID)
    val jsonMessage = quote.toString()
    assertThrows<IllegalArgumentException> { Order.parse(jsonMessage) }
  }

  @Test
  fun `can validate an order`() {
    val order = TestData.getOrder()
    order.sign(TestData.ALICE_DID)

    assertDoesNotThrow { Order.parse(Json.stringify(order)) }
  }
}
