package tbdex.sdk.httpclient

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

/**
 * A singleton object for handling JSON operations,
 * particularly for converting objects into their JSON string representation.
 */
object Json {

  /**
   * An instance of [ObjectMapper] pre-configured for Kotlin support and specific serialization features.
   */
  val objectMapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .findAndRegisterModules()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

  private val objectWriter: ObjectWriter = objectMapper.writer()

  /**
   * Converts the given object into a JSON string.
   *
   * @param obj The object to be converted into a JSON string.
   * @return The JSON string representation of the provided object.
   */
  fun stringify(obj: Any): String {
    return objectWriter.writeValueAsString(obj)
  }
}
