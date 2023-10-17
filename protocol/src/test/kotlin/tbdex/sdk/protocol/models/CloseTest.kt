package tbdex.sdk.protocol.models

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.nimbusds.jose.JWSObject
import org.junit.jupiter.api.assertDoesNotThrow
import tbdex.sdk.protocol.TestData
import tbdex.sdk.protocol.Json
import typeid.TypeID
import kotlin.test.Test
import kotlin.test.assertIs

class CloseTest {
  @Test
  fun `can create a new close`() {
    val close = Close.create(
      to = "pfi",
      from = "alice",
      exchangeId = TypeID(MessageKind.rfq.name),
      closeData = CloseData("my reason")
    )

    assertk.assertAll {
      assertThat(close.metadata.id.prefix).isEqualTo("close")
      assertThat(close.data.reason).isEqualTo("my reason")
    }
  }

  @Test
  fun `sign populates close signature`() {
    val close = TestData.getClose()
    close.sign(TestData.ALICE_DID)

    assertThat(close.signature).isNotNull()
    assertThat(JWSObject.parse(close.signature))
  }

  @Test
  fun `can parse an close from a json string`() {
    val close = TestData.getClose()
    close.sign(TestData.ALICE_DID)
    val jsonMessage = close.toString()
    val parsedMessage = Message.parse(jsonMessage)

    assertIs<Close>(parsedMessage)
    assertThat(parsedMessage.toString()).isEqualTo(jsonMessage)
  }

  @Test
  fun `can validate a close`() {
    val close = TestData.getClose()
    close.sign(TestData.ALICE_DID)

    assertDoesNotThrow { Message.parse(Json.stringify(close)) }
  }
}
