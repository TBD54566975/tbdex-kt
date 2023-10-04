package protocol.models

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import models.Message
import models.ResourceKind
import models.Rfq
import models.RfqData
import models.SelectedPaymentMethod
import protocol.TestData
import typeid.TypeID
import kotlin.test.Test
import kotlin.test.assertIs

class RfqTest {
  @Test
  fun `can create a new rfq`() {
    val rfq = Rfq.create(
      "pfi", "alice",
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
    val jsonMessage = rfq.toJsonString()
    val parsedMessage = Message.parse(jsonMessage)

    assertIs<Rfq>(parsedMessage)
    assertThat(parsedMessage.toJsonString()).isEqualTo(jsonMessage)
  }
}


