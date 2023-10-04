package models

import Mapper
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.module.kotlin.readValue
import dateTimeFormat
import typeid.TypeID
import java.time.OffsetDateTime

// TODO: linter gonna yell at us for this, but I want the typeid and serialization to be ez for now
enum class MessageKind {
  rfq, quote, close, order, orderstatus
}

class MessageMetadata(
  val kind: MessageKind,
  val to: String,
  val from: String,
  val id: TypeID,
  val exchangeId: TypeID,
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimeFormat, timezone = "UTC")
  val createdAt: OffsetDateTime
)


sealed class Message {
  abstract val metadata: MessageMetadata
  abstract val data: MessageData
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

    // TODO detached payload sig check (regenerate payload and then check)
  }

  fun validate() {
    // TODO validate against json schema
//    val schema = schemaMap.get(metadata.kind.name)
//    val jsonString = this.toString()
//    schema.validateBasic(jsonString)
//    if (output.errors != null) ...
  }

  // TODO - use web5 crypto and fix the types
  fun sign(privateKey: String, kid: String) {
    this.signature = "blah"
  }

  fun toJsonString(): String {
    return Mapper.writer().writeValueAsString(this)
  }

  companion object {
    fun parse(payload: String): Message {
      // TODO json schema validation using Message schema

      val node = Mapper.objectMapper.readTree(payload)
      val kind = node.get("metadata").get("kind").asText()

      val kindEnum = MessageKind.valueOf(kind)

      // TODO json schema validation using specific type schema

      return when (kindEnum) {
        MessageKind.rfq -> Mapper.objectMapper.readValue<Rfq>(payload)
        MessageKind.order -> Mapper.objectMapper.readValue<Order>(payload)
        MessageKind.orderstatus -> Mapper.objectMapper.readValue<OrderStatus>(payload)
        MessageKind.quote -> Mapper.objectMapper.readValue<Quote>(payload)
        MessageKind.close -> Mapper.objectMapper.readValue<Close>(payload)
      }
    }
  }
}



