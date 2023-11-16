package tbdex.sdk.protocol

import com.fasterxml.jackson.databind.JsonNode
import tbdex.sdk.protocol.serialization.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

object TestVectors {
  val vectors = readVectors()

  fun readVectors(): JsonNode {
    val stream = URL("https://tbdex.dev/test-vectors/protocol.json").openStream()
    val vectorsJson = BufferedReader(InputStreamReader(stream)).readText()
    return Json.jsonMapper.readTree(vectorsJson)
  }

  fun offering() = vectors["resources"]["offering"].toString()
  fun rfq() = vectors["messages"]["rfq"].toString()
  fun quote() = vectors["messages"]["quote"].toString()
  fun order() = vectors["messages"]["order"].toString()
  fun orderStatus() = vectors["messages"]["orderStatus"].toString()
  fun close() = vectors["messages"]["close"].toString()
}