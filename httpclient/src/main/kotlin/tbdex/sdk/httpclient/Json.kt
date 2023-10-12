package tbdex.sdk.httpclient

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object Json {
  val objectMapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .findAndRegisterModules()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
  private val objectWriter: ObjectWriter = objectMapper.writer()

  /**
   * Converts a kotlin object to a json string.
   *
   * @param obj The object to stringify.
   * @return json string.
   */
  fun stringify(obj: Any): String {
    return objectWriter.writeValueAsString(obj)
  }
}