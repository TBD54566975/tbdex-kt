package tbdex.sdk.protocol

import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.Resource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ResourceVectorTest {
  @Test
  fun `parse offering`() {
    val serialisedOffering = TestVectors.offering()
    val offering = Resource.parse(serialisedOffering)
    assertIs<Offering>(offering)
  }

  @Test
  fun `serialised offering matches original`() {
    val serialisedOffering = TestVectors.offering()
    val offering = Resource.parse(serialisedOffering)
    val serialisedOffering2 = Json.stringify(offering)

    assertEquals(serialisedOffering, serialisedOffering2)
  }

}