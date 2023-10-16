package tbdex.sdk.httpclient

import assertk.assertThat
import assertk.assertions.isNotNull
import kotlin.test.Test

class MockPfiIntegrationTests {
  @Test
  fun `can getOfferings`() {
    val offerings = TbdexHttpClient.getOfferings(GetOfferingsOptions(
      pfiDid = "todo"
    ))

    assertk.assertAll {
      assertThat(offerings).isNotNull()
    }
  }
}