package tbdex.sdk.protocol.models

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import org.junit.jupiter.api.assertDoesNotThrow
import tbdex.sdk.protocol.TestData
import tbdex.sdk.protocol.Json
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
        payinCurrency = CurrencyDetails("AUD"),
        payoutCurrency = CurrencyDetails("BTC"),
        payinMethods = listOf(),
        payoutMethods = listOf(),
        requiredClaims = TestData.getPresentationDefinition()
      )
    )

    assertAll {
      assertThat(offering.data.description).isEqualTo("my fake offering")
      assertThat(offering.metadata.id.prefix).isEqualTo("offering")
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
    offering.sign(TestData.ALICE_DID)

    println(Json.jsonMapper.convertValue<JsonNode>(offering).toPrettyString())

    assertDoesNotThrow { Resource.parse(Json.stringify(offering)) }
  }
}
