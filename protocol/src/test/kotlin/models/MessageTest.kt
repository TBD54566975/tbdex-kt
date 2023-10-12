package protocol.models

import ValidatorException
import com.fasterxml.jackson.core.JsonParseException
import models.Message
import models.Order
import models.Rfq
import org.everit.json.schema.ValidationException
import org.json.JSONException
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
    assertThrows<JSONException> { Message.parse(";;;;") }
  }

  @Test
  fun `parse throws error if message is unsigned`() {
    val exception = assertFailsWith<ValidatorException> {
      Message.parse(Json.stringify(TestData.getQuote()))
    }

    val validationException = exception.cause as ValidationException
    assertContains(validationException.allMessages, "#/signature: expected type: String, found: Null")
  }

  @Test
  fun `parse throws error if message did is invalid`() {
    val exception = assertFailsWith<Exception> {
      Message.parse(Json.stringify(TestData.getOrderStatusWithInvalidDid()))
    }

    val validationException = exception.cause as ValidationException
    assertContains(validationException.allMessages[0], "does not match pattern ^did")
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
      assertDoesNotThrow { Message.parse(Json.stringify(it)) }
    }
  }
}
