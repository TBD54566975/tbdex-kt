package tbdex.sdk.protocol

import com.fasterxml.jackson.databind.JsonNode

object TestVectors {
  val vectors = readVectors()

  fun readVectors(): JsonNode {
    val loader = Thread.currentThread().contextClassLoader
    val vectorsJson = loader.getResourceAsStream("testVectors.json")?.bufferedReader()?.readText()!!
    return Json.jsonMapper.readTree(vectorsJson)
  }

  fun offering() = vectors["resources"]["offering"].toString()
  fun rfq() = vectors["messages"]["rfq"].toString()
  fun quote() = vectors["messages"]["quote"].toString()
  fun order() = vectors["messages"]["order"].toString()
  fun orderStatus() = vectors["messages"]["orderStatus"].toString()
  fun close() = vectors["messages"]["close"].toString()
}