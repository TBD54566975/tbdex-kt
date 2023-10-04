package protocol

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.core.JsonParseException
import models.Message
import models.ResourceKind
import models.Rfq
import models.RfqData
import models.SelectedPaymentMethod
import org.junit.jupiter.api.assertThrows
import typeid.TypeID
import kotlin.test.Test

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
    val jsonMessage = rfq.toJsonString()
    val parsedMessage = Message.parse(jsonMessage) as Rfq

//    val messages = listOf(Message.parse(jsonMessage), Message.parse(jsonMessage))
//    if(messages.last().metadata.kind) {
//      is Rfq -> rfqHandler(Rfq())
//      is Quote -> quoteHandler()
//    }

    assertThat(parsedMessage.toJsonString()).isEqualTo(jsonMessage)
  }

  @Test
  fun `parse throws error if json string is not valid rfq`() {
    assertThrows<JsonParseException> { Message.parse(";;;;") }
  }
}


