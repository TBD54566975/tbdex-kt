package tbdex.sdk.protocol.models

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.startsWith
import org.junit.jupiter.api.assertDoesNotThrow
import tbdex.sdk.protocol.TestData
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.Test
import kotlin.test.assertIs

class OfferingTest {
  @Test
  fun `can create a new offering`() {
    val offering = Offering.create(
      from = TestData.PFI,
      OfferingData(
        description = "my fake offering",
        payoutUnitsPerPayinUnit = "1",
        payin = PayinDetails("AUD"),
        payout = PayinDetails("BTC"),
        payinMethods = listOf(),
        payoutMethods = listOf(),
        requiredClaims = TestData.getPresentationDefinition()
      )
    )

    assertAll {
      assertThat(offering.data.description).isEqualTo("my fake offering")
      assertThat(offering.metadata.id).startsWith("offering")
      assertThat(offering.metadata.protocol).isEqualTo("1.0")
    }
  }

  @Test
  fun `can parse offering from a json string`() {
    val offering = TestData.getOffering()
    offering.sign(TestData.PFI_DID)
    val jsonResource = offering.toString()
    val parsed = Resource.parse(jsonResource)

    assertIs<Offering>(parsed)
    assertThat(parsed.toString()).isEqualTo(jsonResource)
  }

  @Test
  fun `can parse an offering`() {
    val offering = TestData.getOffering()
    offering.sign(TestData.PFI_DID)

    val parsedOffering = assertDoesNotThrow { Resource.parse(Json.stringify(offering)) }
    assertIs<Offering>(parsedOffering)
  }

  @Test
  fun `can parse an offering explicitly`() {
    val offering = TestData.getOffering()
    offering.sign(TestData.PFI_DID)

    val parsedOffering = assertDoesNotThrow { Offering.parse(Json.stringify(offering)) }
    assertIs<Offering>(parsedOffering)
  }
}
