package tbdex.sdk.protocol

import com.networknt.schema.JsonSchema
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.Resource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ResourceVectorTest {
  @Test
  fun `parse offering`() {
    val serializedOffering = TestVectors.offering()
    val offering = Resource.parse(serializedOffering)
    assertIs<Offering>(offering)
  }

  @Test
  fun `serialized offering matches original`() {
    val serializedOffering = TestVectors.offering()
    val offering = Resource.parse(serializedOffering)
    val serializedOffering2 = Json.stringify(offering)

    assertEquals(serializedOffering, serializedOffering2)
  }

}