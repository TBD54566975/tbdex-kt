package website.tbd.tbdex.protocol.message_kinds

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.assertThrows
import typeid.TypeID
import website.tbd.tbdex.protocol.ResourceKind
import kotlin.test.Test
import website.tbd.tbdex.protocol.TestData

class RfqTest {
  val alice = "alice"
  val pfi = "pfi"

  @Test
  fun `can create a new rfq`() {
    val rfq = Rfq.create(
      pfi, alice,
      RfqData(
        offeringID = TypeID(ResourceKind.offering.name),
        payinSubunits = 10_00,
        payinMethod = SelectedPaymentMethod("BTC_ADDRESS", mapOf("address" to 123456)),
        payoutMethod = SelectedPaymentMethod("MOMO", mapOf("phone_number" to 123456)),
        claims = emptyList()
      )
    )
    assertAll {
      assertThat(rfq.metadata.id.prefix).isEqualTo("rfq")
      assertThat(rfq.data.payinSubunits).isEqualTo(10_00)
    }
  }

  @Test
  fun `sign populates rfq signature`() {
    val rfq = TestData.getRfq()
    rfq.sign("fakepk", "fakekid")

    assertThat(rfq.signature).isEqualTo("blah")
  }

  @Test
  fun `can parse an rfq from a json string`() {
    val rfq = TestData.getRfq()
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

