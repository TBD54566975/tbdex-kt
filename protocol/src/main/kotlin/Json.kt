import Json.parse
import Json.stringify
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import typeid.TypeID

const val dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"

class TypeIDToStringSerializer : JsonSerializer<TypeID>() {
  override fun serialize(value: TypeID, gen: JsonGenerator, serializers: SerializerProvider) {
    gen.writeString(value.toString())
  }
}

class StringToTypeIdDeserializer : JsonDeserializer<TypeID>() {
  override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): TypeID {
    return TypeID.fromString(p?.valueAsString).get()
  }
}

/**
 * A singleton for json serialization/deserialization, shared across the SDK as ObjectMapper instantiation
 * is an expensive operation.
 * - Serialize ([stringify])
 * - Deserialize ([parse])
 *
 * ### Example Usage:
 * ```kotlin
 * val offering = Json.objectMapper.readValue<Offering>(payload)
 *
 * val jsonString = Json.stringify(myObject)
 *
 * val node = Json.parse(payload)
 * ```
 */
object Json {
  /**
   * The Jackson object mapper instance, shared across the lib.
   *
   * It must be public in order for typed parsing to work as we cannot use reified types for Java interop.
   */
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

  /**
   * Parses a json string into a Jackson [JsonNode].
   *
   * @param jsonString The json string to parse.
   * @return [JsonNode].
   * @throws JsonParseException if the string is invalid json
   */
  fun parse(jsonString: String): JsonNode {
    return objectMapper.readTree(jsonString)
  }
}
