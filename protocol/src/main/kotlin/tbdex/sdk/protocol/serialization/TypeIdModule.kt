package tbdex.sdk.protocol.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import de.fxlae.typeid.TypeId
import java.io.IOException

/**
 * Jackson [Module] to serialize [TypeId] fields.
 */
class TypeIdModule : SimpleModule() {
  init {
    addSerializer(TypeId::class.java, TypeIdToStringSerializer())
    addDeserializer(TypeId::class.java, StringToTypeIdDeserializer())
  }
}

/**
 * Custom Jackson JSON serializer for converting a [TypeId] object to a JSON string.
 */
class TypeIdToStringSerializer : JsonSerializer<TypeId>() {
  /**
   * Serializes a [TypeId] object to a JSON string.
   *
   * @param value The [TypeId] object to serialize.
   * @param gen The JSON generator to write the serialized value to.
   * @param serializers The serializer provider.
   */
  override fun serialize(value: TypeId, gen: JsonGenerator, serializers: SerializerProvider) {
    gen.writeString(value.toString())
  }
}

/**
 * Custom Jackson JSON deserializer for converting a JSON string to a [TypeId] object.
 */
class StringToTypeIdDeserializer : JsonDeserializer<TypeId>() {
  /**
   * Deserializes a JSON string to a [TypeId] object.
   *
   * @param p The JSON parser containing the JSON string to deserialize.
   * @param ctxt The deserialization context.
   * @return The deserialized [TypeId] object.
   * @throws IOException if there are issues reading the JSON data.
   * @throws JsonParseException if the JSON data is invalid or cannot be deserialized.
   */
  override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): TypeId {
    return TypeId.parse(p?.valueAsString)
  }
}