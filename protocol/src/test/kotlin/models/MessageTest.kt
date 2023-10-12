package protocol.models

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isNotNull
import com.fasterxml.jackson.core.JsonParseException
import com.nimbusds.jose.JWSObject
import models.Message
import models.Order
import models.Rfq
import org.junit.jupiter.api.assertDoesNotThrow
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
    assertThrows<JsonParseException> { Message.parse(";;;;") }
  }
}