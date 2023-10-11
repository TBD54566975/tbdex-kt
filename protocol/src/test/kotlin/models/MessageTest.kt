package protocol.models

import com.fasterxml.jackson.core.JsonParseException
import models.Message
import models.Order
import models.Rfq
import org.junit.jupiter.api.assertThrows
import protocol.TestData
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

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
    val messages = listOf(rfq.toJson(), order.toJson()).map { Message.parse(it) }

    assertIs<Rfq>(messages.first())
    assertIs<Order>(messages.last())
  }

  @Test
  fun `parse throws error if json string is not valid`() {
    assertThrows<JsonParseException> { Message.parse(";;;;") }
  }

  @Test
  fun `can validate a list of messages`() {
    val rfq = TestData.getRfq()
    rfq.sign("fakepk", "fakekid")
    val order = TestData.getOrder()
    order.sign("fakepk", "fakekid")
    listOf(Json.stringify(rfq), Json.stringify(order)).map {
      try {
        Message.validate(it)
      } catch (e: Exception) {
        throw e
      }
    }
  }

  @Test
  fun `validate throws error if did is not valid`() {
    val exception = assertFailsWith<Exception> {
      Message.validate(Json.stringify(TestData.getRfq()))
    }
    assertTrue(exception.message?.contains("does not match pattern ^did") == true)
  }
}