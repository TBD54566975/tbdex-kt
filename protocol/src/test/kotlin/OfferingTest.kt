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
    offering.sign()

    assertThat(offering.signature).isEqualTo("blah")
  }
}