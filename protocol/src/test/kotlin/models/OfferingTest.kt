package protocol.models

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import models.CurrencyDetails
import models.Offering
import models.OfferingData
import models.PresentationExchange
import models.Resource
import protocol.TestData
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertIs

class OfferingTest {
  @Test
  fun `can create a new offering`() {
    val offering = Offering.create(
      from = "from",
      OfferingData(
        description = "my fake offering",
        payoutUnitsPerPayinUnit = 1,
        payinCurrency = CurrencyDetails("AUD"),
        payoutCurrency = CurrencyDetails("BTC"),
        payinMethods = listOf(),
        payoutMethods = listOf(),
        requiredClaims = PresentationExchange()
      )
    )

    assertAll {
      assertContains(offering.metadata.id, "offering")
      assertThat(offering.data.description).isEqualTo("my fake offering")
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
    val jsonResource = offering.toJson()
    val parsed = Resource.parse(jsonResource)

    assertIs<Offering>(parsed)
    assertThat(parsed.toJson()).isEqualTo(jsonResource)
  }

  @Test
  fun `can validate an offering`() {
    val offering = TestData.getOffering()
    offering.sign("fakepk", "fakekid")

    try {
      Resource.validate(Json.stringify(offering))
    } catch (e: Exception) {
      throw e
    }
  }
}