package models

import Json
import Json.objectMapper
import StringToTypeIdDeserializer
import TypeIDToStringSerializer
import Validator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import dateTimeFormat
import org.json.JSONObject
import typeid.TypeID
import java.time.OffsetDateTime

/**
 * An enum representing all possible [Message] kinds.
 */
@Suppress("EnumNaming")
enum class MessageKind {
  // TODO: linter gonna yell at us for this, but I want the typeid and serialization to be ez for now
  rfq, quote, close, order, orderstatus
}

/**
 * A data class representing the metadata present on every [Message].
 */
class MessageMetadata(
  val kind: MessageKind,
  val to: String,
  val from: String,
  @JsonSerialize(using = TypeIDToStringSerializer::class)
  @JsonDeserialize(using = StringToTypeIdDeserializer::class)
  val id: TypeID,
  @JsonDeserialize(using = StringToTypeIdDeserializer::class)
  @JsonSerialize(using = TypeIDToStringSerializer::class)
  val exchangeId: TypeID,
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimeFormat, timezone = "UTC")
  val createdAt: OffsetDateTime
)

/**
 * An abstract class representing the structure and common functionality available on all Messages.
 */
sealed class Message {
  abstract val metadata: MessageMetadata
  abstract val data: MessageData
  abstract var signature: String?

  /**
   * Signs the message, excluding the Rfq.private field if present,
   * as a detached payload JWT, using the private key and kid provided.
   *
   * @param privateKey The private key used to sign the message.
   * @param kid The kid used to sign the message
   */
  // TODO - use web5 crypto and fix the types
  fun sign(privateKey: String, kid: String) {
    this.signature = "blah"
  }

  /**
   * Uses [Json] to serialize the Message as a json string.
   *
   * @return The json string
   */
  fun toJson(): String {
    return Json.stringify(this)
  }

  companion object {
    /**
     * Takes an existing Message in the form of a json string and parses it into a Message object.
     * Validates object structure and performs an integrity check using the message signature.
     *
     * @param payload The message as a json string.
     * @return The json string parsed into a concrete Message implementation.
     * @throws IllegalArgumentException if the payload is not valid json.
     * @throws IllegalArgumentException if the payload does not conform to the expected json schema.
     * @throws IllegalArgumentException if the payload signature verification fails.
     */
    fun parse(payload: String): Message {
      val messageJson = JSONObject(payload)

      // validate message structure
      Validator.validate(messageJson, "message")

      val dataJson = messageJson.getJSONObject("data")
      val kind = messageJson.getJSONObject("metadata").getString("kind")

      // validate specific message data (Rfq, Quote, etc)
      Validator.validate(dataJson, kind)

      val messageType = when (MessageKind.valueOf(kind)) {
        MessageKind.rfq -> Rfq::class.java
        MessageKind.order -> Order::class.java
        MessageKind.orderstatus -> OrderStatus::class.java
        MessageKind.quote -> Quote::class.java
        MessageKind.close -> Close::class.java
      }

      return objectMapper.convertValue(messageJson, messageType)
    }
    
    /**
     * Verifies the cryptographic integrity of the message's signature.
     *
     * @throws Exception TODO link to crypto method throws
     */
    fun verify() {
      // TODO detached payload sig check (regenerate payload and then check)
    }
  }
}



