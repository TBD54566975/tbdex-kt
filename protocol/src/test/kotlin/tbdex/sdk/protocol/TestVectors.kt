package tbdex.sdk.protocol

import com.fasterxml.jackson.databind.JsonNode

object TestVectors {
  val vectors = readVectors()

  fun readVectors(): JsonNode {
    val loader = Thread.currentThread().contextClassLoader
    val vectorsJson = loader.getResourceAsStream("testVectors.json")?.bufferedReader()?.readText()!!
    return Json.parse(vectorsJson)
  }

  fun offering(): String {
    return vectors["resources"]["offering"].toString()
  }
}