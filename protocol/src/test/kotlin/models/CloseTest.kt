package models

import assertk.assertThat
import assertk.assertions.isEqualTo
import protocol.TestData
import typeid.TypeID
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CloseTest {
  @Test
  fun `can create a new close`() {
    val close = Close.create("pfi", "alice", TypeID(MessageKind.rfq.name), CloseData("my reason"))

    assertk.assertAll {
      assertThat(close.metadata.id.prefix).isEqualTo("close")
      assertThat(close.data.reason).isEqualTo("my reason")
    }
  }

  @Test
  fun `sign populates close signature`() {
    val close = TestData.getClose()
    close.sign("fakepk", "fakekid")

    assertThat(close.signature).isEqualTo("blah")
  }

  @Test
  fun `can parse an close from a json string`() {
    val close = TestData.getClose()
    close.sign("fakepk", "fakekid")
    val jsonMessage = close.toJson()
    val parsedMessage = Message.parse(jsonMessage)

    assertIs<Close>(parsedMessage)
    assertThat(parsedMessage.toJson()).isEqualTo(jsonMessage)
  }

  @Test
  fun `can validate a close`() {
    val close = TestData.getClose()
    close.sign("fakepk", "fakekid")
    // TODO: find a better way to assert no exceptions
    assertIs<Unit>(Message.validate(Json.stringify(close)))
  }

}