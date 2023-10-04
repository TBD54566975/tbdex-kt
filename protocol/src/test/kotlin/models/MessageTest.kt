package protocol.models

import com.fasterxml.jackson.core.JsonParseException
import models.Message
import models.Order
import models.Rfq
import org.junit.jupiter.api.assertThrows
import protocol.TestData
import kotlin.test.Test
import kotlin.test.assertIs

class MessageTest {
  @Test
  fun `can create a list of messages with multiple types`() {
    val rfq = TestData.getRfq()
    val order = TestData.getOrder()

    assertIs<List<Message>>(listOf(rfq, order))
  }

  @Test
  fun `can parse a list of messages`() {
    val rfq = TestData.getRfq()
    val order = TestData.getOrder()
    val messages = listOf(rfq.toJsonString(), order.toJsonString()).map { Message.parse(it) }

    assertIs<Rfq>(messages.first())
    assertIs<Order>(messages.last())
  }

  @Test
  fun `parse throws error if json string is not valid`() {
    assertThrows<JsonParseException> { Message.parse(";;;;") }
  }
}