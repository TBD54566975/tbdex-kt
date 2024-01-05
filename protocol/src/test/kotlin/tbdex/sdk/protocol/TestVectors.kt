package tbdex.sdk.protocol

import com.fasterxml.jackson.databind.JsonNode
import tbdex.sdk.protocol.serialization.Json

object TestVectors {
//  val vectors = readVectors()
//
//  fun readVectors(): JsonNode {
//    val loader = Thread.currentThread().contextClassLoader
//    val vectorsJson = loader.getResourceAsStream("testVectors.json")?.bufferedReader()?.readText()!!
//    return Json.jsonMapper.readTree(vectorsJson)
//  }

  fun offering(): String {
    val loader = Thread.currentThread().contextClassLoader
    return loader.getResourceAsStream("test-vectors/parse-offering.json")?.bufferedReader()?.readText()!!
  }
  fun rfq(): String {
    val loader = Thread.currentThread().contextClassLoader
    return loader.getResourceAsStream("test-vectors/parse-rfq.json")?.bufferedReader()?.readText()!!
  }
  fun quote(): String {
    val loader = Thread.currentThread().contextClassLoader
    return loader.getResourceAsStream("test-vectors/parse-quote.json")?.bufferedReader()?.readText()!!
  }
  fun order(): String {
    val loader = Thread.currentThread().contextClassLoader
    return loader.getResourceAsStream("test-vectors/parse-order.json")?.bufferedReader()?.readText()!!
  }
  fun orderStatus(): String {
    val loader = Thread.currentThread().contextClassLoader
    return loader.getResourceAsStream("test-vectors/parse-orderstatus.json")?.bufferedReader()?.readText()!!
  }
  fun close(): String {
    val loader = Thread.currentThread().contextClassLoader
    return loader.getResourceAsStream("test-vectors/parse-close.json")?.bufferedReader()?.readText()!!
  }
}