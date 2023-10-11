package models

import Json
import Json.objectMapper
import StringToTypeIdDeserializer
import TypeIDToStringSerializer
import Validator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.readValue
import dateTimeFormat
import org.json.JSONObject
import typeid.TypeID
import java.time.OffsetDateTime


enum class ResourceKind {
  offering
}

class ResourceMetadata(
  val kind: ResourceKind,
  val from: String,
  @JsonSerialize(using = TypeIDToStringSerializer::class)
  @JsonDeserialize(using = StringToTypeIdDeserializer::class)
  val id: TypeID,
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimeFormat, timezone = "UTC")
  val createdAt: OffsetDateTime,
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimeFormat, timezone = "UTC")
  val updatedAt: OffsetDateTime?
)

sealed class Resource {
  abstract val metadata: ResourceMetadata
  abstract val data: ResourceData
  abstract var signature: String?

  fun sign(privateKey: String, kid: String) {
    this.signature = "blah"
  }

  fun toJson(): String {
    return Json.stringify(this)
  }

  companion object {
    fun parse(payload: String): Resource {
      // TODO json schema validation using Resource schema

      val node = Json.parse(payload)
      val kind = node.get("metadata").get("kind").asText()

      val kindEnum = ResourceKind.valueOf(kind)

      // TODO json schema validation using specific type schema

      return when (kindEnum) {
        ResourceKind.offering -> objectMapper.readValue<Offering>(payload)
//        ResourceKind.reputation -> TODO()
      }
    }

    fun verify() {
      // TODO detached payload sig check (regenerate payload and then check)
    }

    fun validate(resource: String) {
      val resourceJson = JSONObject(resource)
      // validate message structure
      Validator.validate(resourceJson, "resource")

      val dataJson = resourceJson.getJSONObject("data")
      val metadataJson = resourceJson.getJSONObject("metadata")
      val kind = metadataJson.getString("kind")
      // validate specific resource data
      Validator.validate(dataJson, kind)
    }
  }
}