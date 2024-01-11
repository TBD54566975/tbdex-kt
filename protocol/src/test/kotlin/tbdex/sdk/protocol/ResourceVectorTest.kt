package tbdex.sdk.protocol

import com.fasterxml.jackson.databind.JsonNode
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.Resource
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class ResourceVectorTest {
  @Test
  fun `parse-offering json`() {
    val vector = TestVectors.getVector("parse-offering.json")
    assertNotNull(vector)
    testNonErrorTestVector<Offering>(vector)
  }

  private inline fun <reified T> testNonErrorTestVector(vector: JsonNode) {
    val input = vector["input"].textValue()
    assertNotNull(input)

    val tbDEXMessage = Resource.parse(input)
    assertIs<T>(tbDEXMessage)

    assertEquals(vector["output"], Json.jsonMapper.readTree(tbDEXMessage.toString()))
  }
}