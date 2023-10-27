package tbdex.sdk.protocol.models

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isNotNull
import com.nimbusds.jose.JWSObject
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import tbdex.sdk.protocol.TestData
import tbdex.sdk.protocol.ValidatorException
import tbdex.sdk.protocol.serialization.Json
import java.security.SignatureException
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
    val exception = assertThrows<IllegalArgumentException> { Message.parse(";;;;") }
    assertThat(exception.message!!).contains("unexpected character at offset")
  }

  @Test
  fun `parse throws error if message is unsigned`() {
    val exception = assertFailsWith<ValidatorException> {
      Message.parse(Json.stringify(TestData.getQuote()))
    }

    assertContains(exception.errors, "$.signature: is missing but it is required")
  }

  @Test
  fun `parse throws error if message did is invalid`() {
    val exception = assertFailsWith<ValidatorException> {
      Message.parse(Json.stringify(TestData.getOrderStatusWithInvalidDid()))
    }

    assertContains(exception.errors[0], "does not match the regex pattern ^did")
  }

  @Test
  fun `can validate a list of messages`() {
    val rfq = TestData.getRfq()
    val quote = TestData.getQuote()
    val order = TestData.getOrder()
    rfq.sign(TestData.ALICE_DID)
    quote.sign(TestData.PFI_DID)
    order.sign(TestData.ALICE_DID)

    listOf(rfq, quote, order).map {
      assertDoesNotThrow { Message.parse(Json.stringify(it)) }
    }
  }

  @Test
  fun `messages must be signed by the sender`() {
    val rfqFromAlice = TestData.getRfq()
    //sign it with the wrong DID
    rfqFromAlice.sign(TestData.PFI_DID)

    val exception = assertThrows<SignatureException> {
      Message.parse(Json.stringify(rfqFromAlice))
    }
    assertThat(exception.message!!).contains("Signature verification failed: Was not signed by the expected DID")
  }
}
