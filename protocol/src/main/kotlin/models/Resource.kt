package models

import Mapper
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.module.kotlin.readValue
import dateTimeFormat
import typeid.TypeID
import java.time.OffsetDateTime


enum class ResourceKind {
  offering,
  reputation
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

sealed interface ResourceData

open class Resource<T : ResourceData>(
  val metadata: ResourceMetadata,
  val data: T,
  var signature: String? = null
) {
  init {
    when (metadata.kind) {
      ResourceKind.offering -> require(data is OfferingData)
      ResourceKind.reputation -> TODO()
    }

    if (signature != null) {
      verify()
    } else {
      validate()
    }
  }


  private fun verify() {
    validate()

    // TODO sig check
  }

  private fun validate() {
    // TODO validate against json schema
//    val schema = schemaMap.get(metadata.kind.name)
//    val jsonString = this.toString()
//    schema.validateBasic(jsonString)
//    if (output.errors != null) ...
  }

  fun sign(privateKey: String, kid: String) {
    this.signature = "blah"
  }

  override fun toString(): String {
    return Mapper.writer().writeValueAsString(this)
  }

  companion object {
    fun parse(payload: String): Resource<out ResourceData> {
      // TODO json schema validation using Resource schema

      val node = Mapper.objectMapper.readTree(payload)
      val kind = node.get("metadata").get("kind").asText()

      val kindEnum = ResourceKind.valueOf(kind)

      // TODO json schema validation using specific type schema

      return when (kindEnum) {
        ResourceKind.offering -> Mapper.objectMapper.readValue<Resource<OfferingData>>(payload)
        ResourceKind.reputation -> TODO()
      }
    }
  }
}