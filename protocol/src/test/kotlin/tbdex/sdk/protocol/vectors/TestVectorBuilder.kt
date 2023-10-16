package tbdex.sdk.protocol.vectors

import java.io.File
import protocol.tbdex.sdk.protocol.TestData
import tbdex.sdk.protocol.Json
import tbdex.sdk.protocol.models.Offering

fun main() {
  TestVectorBuilder.writeTestVectors()
}

/**
 * Utility to produce the tbDEX test vectors JSON file
 */
object TestVectorBuilder {
  fun writeTestVectors() {
    val vectors = buildTestVectors()
    val json = Json.stringify(vectors)
    println(json)
    //write to test/resources/testVectors.json
  }

  fun buildTestVectors(): Map<String, Map<String, Any>> {
    return mapOf(
      "resources" to mapOf(
        "offering" to offering()
      )
    )
  }

  fun offering(): Any {
    val offering = TestData.getOffering()
    offering.sign(TestData.PFI_DID)
    return offering
  }
}