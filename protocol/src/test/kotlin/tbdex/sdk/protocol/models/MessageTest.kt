package tbdex.sdk.protocol.models

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isNotNull
import com.nimbusds.jose.JWSObject
import org.everit.json.schema.ValidationException
import org.json.JSONException
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import protocol.tbdex.sdk.protocol.TestData
import tbdex.sdk.protocol.Json
import tbdex.sdk.protocol.ValidatorException
import kotlin.test.Test
import kotlin.test.assertContains
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
    rfq.sign(TestData.ALICE_DID)
    val order = TestData.getOrder()
    order.sign(TestData.ALICE_DID)
    val messages = listOf(rfq.toString(), order.toString()).map { Message.parse(it) }

    assertIs<Rfq>(messages.first())
    assertIs<Order>(messages.last())
  }

  @Test
  fun `sign populates message signature`() {
    val rfq = TestData.getRfq()
    rfq.sign(TestData.PFI_DID)

    assertAll {
      assertThat(rfq.signature).isNotNull()
      val jws = assertDoesNotThrow { JWSObject.parse(rfq.signature) }
      assertThat(jws.header.algorithm).isNotNull()
      assertThat(jws.header.keyID).contains(TestData.PFI_DID.uri)
    }
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
    rfq.sign(TestData.ALICE_DID)
    quote.sign(TestData.ALICE_DID)
    order.sign(TestData.ALICE_DID)

    listOf(rfq, quote, order).map {
      assertDoesNotThrow { Message.parse(Json.stringify(it)) }
    }
  }
}
