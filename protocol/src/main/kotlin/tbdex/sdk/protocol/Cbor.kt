package tbdex.sdk.protocol

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import tbdex.sdk.protocol.serialization.TypeIdModule

/**
 * Object providing a configured ObjectMapper for CBOR (Concise Binary Object Representation) serialization.
 */
object Cbor {
  /**
   * An instance of ObjectMapper configured for CBOR serialization.
   * - Registers the Kotlin module for Kotlin-specific features.
   * - Automatically finds and registers all Jackson modules.
   * - Disables the serialization of dates as timestamps.
   */
  val cborMapper: ObjectMapper = CBORMapper()
    .registerKotlinModule()
    .findAndRegisterModules()
    .registerModule(TypeIdModule())
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
}
