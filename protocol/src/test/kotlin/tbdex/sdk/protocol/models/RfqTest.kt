package tbdex.sdk.protocol.models

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import tbdex.sdk.protocol.TestData
import tbdex.sdk.protocol.serialization.Json
import typeid.TypeID
import kotlin.test.Test
import kotlin.test.assertIs

class RfqTest {
  @Test
  fun `can create a new rfq`() {
    val rfq = Rfq.create(
      TestData.PFI, TestData.ALICE,
      RfqData(
        offeringId = TypeID(ResourceKind.offering.name),
        payinSubunits = "1000",
        payinMethod = SelectedPaymentMethod("BTC_ADDRESS", mapOf("address" to 123456)),
        payoutMethod = SelectedPaymentMethod("MOMO", mapOf("phone_number" to 123456)),
        claims = emptyList()
      )
    )

    assertAll {
      assertThat(rfq.metadata.id.prefix).isEqualTo("rfq")
      assertThat(rfq.data.payinSubunits).isEqualTo("1000")
    }
  }

  @Test
  fun `can parse an rfq from a json string`() {
    val rfq = TestData.getRfq()
    rfq.sign(TestData.ALICE_DID)
    val jsonMessage = rfq.toString()
    val parsedMessage = Message.parse(jsonMessage)

    assertIs<Rfq>(parsedMessage)
    assertThat(parsedMessage.toString()).isEqualTo(jsonMessage)
  }

  @Test
  fun `can validate a rfq`() {
    val rfq = TestData.getRfq()
    rfq.sign(TestData.ALICE_DID)

    assertDoesNotThrow { Message.parse(Json.stringify(rfq)) }
  }

  @Test
  @Disabled
  fun `verifyOfferingRequirements succeeds when claims satisfy pd`() {
    val offering = TestData.getOffering()
    val rfq = TestData.getRfq(offering.metadata.id, listOf(TestData.getVC().toString()))

    assertDoesNotThrow { rfq.verifyOfferingRequirements(offering) }
  }

  @Test
  @Disabled
  fun `verifyOfferingRequirements throws when claims do not satisfy pd`() {
    val offering = TestData.getOffering()
    val rfq = TestData.getRfq()

    assertThrows<IllegalArgumentException> { rfq.verifyOfferingRequirements(offering) }
  }

  @Test
  @Disabled
  fun `verifyOfferingRequirements throws when claims fail verification`() {
    val offering = TestData.getOffering()
    val rfq = TestData.getRfq()

    // distinguish that this is a verification failure
    assertThrows<IllegalArgumentException> { rfq.verifyOfferingRequirements(offering) }
  }
}


