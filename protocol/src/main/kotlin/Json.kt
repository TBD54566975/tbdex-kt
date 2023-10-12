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

/**
 * Custom Jackson JSON serializer for converting a [TypeID] object to a JSON string.
 * This serializer is used to customize the JSON serialization of [TypeID] instances.
 *
 * @see TypeID
 */
class TypeIDToStringSerializer : JsonSerializer<TypeID>() {
  /**
   * Serializes a [TypeID] object to a JSON string.
   *
   * @param value The [TypeID] object to serialize.
   * @param gen The JSON generator to write the serialized value to.
   * @param serializers The serializer provider.
   */
  override fun serialize(value: TypeID, gen: JsonGenerator, serializers: SerializerProvider) {
    gen.writeString(value.toString())
  }
}

/**
 * Custom Jackson JSON deserializer for converting a JSON string to a [TypeID] object.
 * This deserializer is used to customize the JSON deserialization of [TypeID] instances.
 *
 * @see TypeID
 */
class StringToTypeIdDeserializer : JsonDeserializer<TypeID>() {
  /**
   * Deserializes a JSON string to a [TypeID] object.
   *
   * @param p The JSON parser containing the JSON string to deserialize.
   * @param ctxt The deserialization context.
   * @return The deserialized [TypeID] object.
   * @throws IOException if there are issues reading the JSON data.
   * @throws JsonParseException if the JSON data is invalid or cannot be deserialized.
   */
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
  val jsonMapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .findAndRegisterModules()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

  private val objectWriter: ObjectWriter = jsonMapper.writer()

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
    return jsonMapper.readTree(jsonString)
  }
}
