package protocol.models

import com.fasterxml.jackson.core.JsonParseException
import models.Message
import models.Order
import models.Rfq
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import protocol.TestData
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
    rfq.sign("fakepk", "fakedid")
    val order = TestData.getOrder()
    order.sign("fakepk", "fakedid")

    val messages = listOf(rfq.toJson(), order.toJson()).map { Message.parse(it) }

    assertIs<Rfq>(messages.first())
    assertIs<Order>(messages.last())
  }

  @Test
  fun `parse throws error if json string is not valid`() {
    assertThrows<JsonParseException> { Message.parse(";;;;") }
  }

  @Test
  fun `validate throws error if message is unsigned`() {
    val exception = assertFailsWith<Exception> {
      Message.validate(Json.stringify(TestData.getQuote()))
    }
    exception.message?.let { assertEquals("JSON schema validation failed, errors: [Validation Error:\n" +
      "Message: #/signature: expected type: String, found: Null\n" +
      "Pointer to Violation: #/signature\n" +
      "Schema Location: classpath:/#/properties/signature]", it) }
  }

  @Test
  fun `validate throws error if message did is invalid`() {
    val exception = assertFailsWith<Exception> {
      Message.validate(Json.stringify(TestData.getOrderStatusWithInvalidDid()))
    }
    exception.message?.let { assertContains(it, "does not match pattern ^did") }
  }

  @Test
  fun `can validate a list of messages`() {
    val rfq = TestData.getRfq()
    val quote = TestData.getQuote()
    val order = TestData.getOrder()
    rfq.sign("fakepk", "fakekid")
    quote.sign("fakepk", "fakekid")
    order.sign("fakepk", "fakekid")

    listOf(rfq, quote, order).map {
      assertDoesNotThrow { Message.validate(Json.stringify(it)) }
    }
  }
}
