package models

import Json
import Json.objectMapper
import Validator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.module.kotlin.readValue
import dateTimeFormat
import org.json.JSONObject
import typeid.TypeID
import java.time.OffsetDateTime

// TODO: linter gonna yell at us for this, but I want the typeid and serialization to be ez for now
enum class MessageKind {
  rfq, quote, close, order, orderstatus
}

// TODO: use jackson serializer to get string for id/exchangeId
class MessageMetadata(
  val kind: MessageKind,
  val to: String,
  val from: String,
  val id: String,
  val exchangeId: String,
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimeFormat, timezone = "UTC")
  val createdAt: OffsetDateTime
)

sealed class Message {
  abstract val metadata: MessageMetadata
  abstract val data: MessageData
  abstract var signature: String?

  // TODO - use web5 crypto and fix the types
  fun sign(privateKey: String, kid: String) {
    this.signature = "blah"
  }

  fun toJson(): String {
    return Json.stringify(this)
  }

  companion object {
    fun parse(payload: String): Message {
      // TODO json schema validation using Message schema

      val node = Json.parse(payload)
      val kind = node.get("metadata").get("kind").asText()

      val kindEnum = MessageKind.valueOf(kind)

      // TODO json schema validation using specific type schema

      return when (kindEnum) {
        MessageKind.rfq -> objectMapper.readValue<Rfq>(payload)
        MessageKind.order -> objectMapper.readValue<Order>(payload)
        MessageKind.orderstatus -> objectMapper.readValue<OrderStatus>(payload)
        MessageKind.quote -> objectMapper.readValue<Quote>(payload)
        MessageKind.close -> objectMapper.readValue<Close>(payload)
      }
    }
    fun verify() {
      // TODO detached payload sig check (regenerate payload and then check)
    }

    // js version takes any for jsonMessage
    fun validate(message: String) {
      val messageJson = JSONObject(message)

      // validate message structure
      Validator.validate(messageJson, "message")

      val dataJson = messageJson.getJSONObject("data")
      val kind = messageJson.getJSONObject("metadata").getString("kind")

      // validate specific message data (Rfq, Quote, etc)
      Validator.validate(dataJson, kind)
    }
  }
}



