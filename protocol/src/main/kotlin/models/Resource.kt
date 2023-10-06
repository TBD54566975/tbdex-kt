package models

import Json
import Json.objectMapper
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.module.kotlin.readValue
import dateTimeFormat
import typeid.TypeID
import java.time.OffsetDateTime


enum class ResourceKind {
  offering
}

class ResourceMetadata(
  val kind: ResourceKind,
  val from: String,
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

  init {
    if (signature != null) {
      verify()
    } else {
      validate()
    }
  }

  fun verify() {
    validate()

    // TODO sig check
  }

  fun validate() {
    // TODO validate against json schema
//    val schema = schemaMap.get(metadata.kind.name)
//    val jsonString = this.toString()
//    schema.validateBasic(jsonString)
//    if (output.errors != null) ...
  }

  fun sign(privateKey: String, kid: String) {
    this.signature = "blah"
  }

  fun toJsonString(): String {
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
  }
}