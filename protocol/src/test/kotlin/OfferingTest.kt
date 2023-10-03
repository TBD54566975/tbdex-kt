package protocol

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import models.CurrencyDetails
import models.Offering
import models.OfferingData
import models.PresentationExchange
import kotlin.test.Test

class OfferingTest {
  @Test
  fun `can create a new offering`() {
    val offering = Offering.create(
      from = "from",
      OfferingData(
        description = "my fake offering",
        payoutUnitsPerPayinUnit = 1,
        payinCurrency = CurrencyDetails("", "", ""),
        payoutCurrency = CurrencyDetails("", "", ""),
        payinMethods = listOf(),
        payoutMethods = listOf(),
        requiredClaims = PresentationExchange()
      )
    )

    assertAll {
      assertThat(offering.data.description).isEqualTo("my fake offering")
      assertThat(offering.metadata.id.prefix).isEqualTo("offering")
    }
  }

  @Test
  fun `sign populates offering signature`() {
    val offering = TestData.getOffering()
    offering.sign("fakepk", "fakekid")

    assertThat(offering.signature).isEqualTo("blah")
  }

  @Test
  fun `can parse offering from a json string`() {
    val offering = TestData.getOffering()
    offering.sign("fakepk", "fakekid")
    val jsonResource = offering.toString()
    val parsed = Offering.parse(jsonResource)

    assertThat(parsed.toString()).isEqualTo(jsonResource)
  }
}