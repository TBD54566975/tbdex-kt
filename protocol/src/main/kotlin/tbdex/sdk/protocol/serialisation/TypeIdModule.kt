package tbdex.sdk.protocol.serialisation

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import typeid.TypeID
import java.io.IOException

/**
 * Jackson [Module] the serialise [TypeID] fields.
 */
class TypeIdModule: SimpleModule() {
  init {
    addSerializer(TypeID::class.java, TypeIDToStringSerializer())
    addDeserializer(TypeID::class.java, StringToTypeIdDeserializer())
  }
}

/**
 * Custom Jackson JSON serializer for converting a [TypeID] object to a JSON string.
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