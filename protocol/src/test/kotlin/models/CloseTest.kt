package models

import assertk.assertThat
import assertk.assertions.isEqualTo
import protocol.TestData
import typeid.TypeID
import kotlin.test.Test
import kotlin.test.assertIs

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
    close.sign(TestData.ALICE_DID)

    assertThat(close.signature).isEqualTo("blah")
  }

  @Test
  fun `can parse an close from a json string`() {
    val close = TestData.getClose()
    close.sign(TestData.ALICE_DID)
    val jsonMessage = close.toJson()
    val parsedMessage = Message.parse(jsonMessage)

    assertIs<Close>(parsedMessage)
    assertThat(parsedMessage.toJson()).isEqualTo(jsonMessage)
  }

  @Test
  fun `can parse a close from a fake signed rfq`() {
    val close = TestData.getClose()
    close.signature =
      "eyJhbGciOiJFZERTQSIsImtpZCI6ImRpZDprZXk6ejZNa291Z0NjemdrNk1ySnExS2N3U1RZNTZRTThxYnpNZFBCZDM3WWZxU1FRcFJXI3o2TWtvdWdDY3pnazZNckpxMUtjd1NUWTU2UU04cWJ6TWRQQmQzN1lmcVNRUXBSVyJ9..s92GEPulVDmV3lf9XLS7qIw16VgxjZhrEu5rvwoBvTXk9gdgNuDSGgFfFQMk5HcpurPC0MeuoDpLLbQNK0elCw"

    val jsonMessage = close.toJson()
    val parsedMessage = Message.parse(jsonMessage)
    assertIs<Close>(parsedMessage)
    assertThat(parsedMessage.toJson()).isEqualTo(jsonMessage)
  }
}