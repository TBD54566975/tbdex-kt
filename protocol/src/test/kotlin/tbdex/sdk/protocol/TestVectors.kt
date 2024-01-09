package tbdex.sdk.protocol

import com.fasterxml.jackson.databind.JsonNode
import tbdex.sdk.protocol.serialization.Json

object TestVectors {
  val vectors = readVectors()

  fun readVectors(): MutableMap<String, JsonNode> {
    val loader = Thread.currentThread().contextClassLoader
    val vectors = mutableMapOf<String, JsonNode>();
    val vectorFiles = arrayOf(
      "parse-close.json",
      "parse-offering.json",
      "parse-order.json",
      "parse-orderstatus.json",
      "parse-quote.json",
      "parse-rfq.json"
    )
    for (vectorFile in vectorFiles) {
      val vectorJson = loader.getResourceAsStream("test-vectors/$vectorFile")?.bufferedReader()?.readText()!!
      vectors[vectorFile] = Json.jsonMapper.readTree(vectorJson)
    }
    return vectors
  }

  fun getVector(vectorFile: String): JsonNode? {
    return vectors[vectorFile]
  }
}