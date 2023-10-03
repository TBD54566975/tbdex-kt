package protocol

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.core.JsonParseException
import models.Rfq
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class RfqTest {
  val alice = "alice"
  val pfi = "pfi"

  @Test
  fun `can create a new rfq`() {
    val rfq = Rfq.create(pfi, alice, 13)
    assertAll {
      assertThat(rfq.metadata.id.prefix).isEqualTo("rfq")
      assertThat(rfq.data.amount).isEqualTo(13)
    }
  }

  @Test
  fun `sign populates rfq signature`() {
    val rfq = Rfq.create(pfi, alice, 13)
    rfq.sign("fakepk", "fakekid")

    assertThat(rfq.signature).isEqualTo("blah")
  }

  @Test
  fun `can parse an rfq from a json string`() {
    val rfq = Rfq.create(pfi, alice, 13)
    rfq.sign("fakepk", "fakekid")
    val jsonMessage = rfq.toString()
    val parsedMessage = Rfq.parse(jsonMessage)

    assertThat(parsedMessage.toString()).isEqualTo(jsonMessage)
  }

  @Test
  fun `parse throws error if json string is not valid rfq`() {
    assertThrows<JsonParseException> { Rfq.parse(";;;;") }
  }
}


