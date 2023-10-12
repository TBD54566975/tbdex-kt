package tbdex.sdk.protocol.models

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.json.JSONObject
import tbdex.sdk.protocol.CryptoUtils
import tbdex.sdk.protocol.Json
import tbdex.sdk.protocol.Json.jsonMapper
import tbdex.sdk.protocol.StringToTypeIdDeserializer
import tbdex.sdk.protocol.TypeIDToStringSerializer
import tbdex.sdk.protocol.Validator
import tbdex.sdk.protocol.dateTimeFormat
import typeid.TypeID
import web5.sdk.dids.Did
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
   * Signs the Message using the specified [did] and optionally the given [keyAlias].
   *
   * @param did The DID (Decentralized Identifier) used for signing.
   * @param keyAlias The alias of the key to be used for signing (optional).
   * @throws Exception if the signing operation fails.
   */
  fun sign(did: Did, keyAlias: String? = null) {
    val payload = mapOf("metadata" to this.metadata, "data" to this.data)
    this.signature = CryptoUtils.sign(did = did, payload = payload, assertionMethodId = keyAlias)
  }

  /**
   * Verifies the signature of the data.
   *
   * This function verifies the signature using the previously set [signature] property.
   * It compares the signature against a hashed payload consisting of metadata and data.
   *
   * @throws Exception if the verification fails or if the signature is missing.
   */
  private fun verify() {
    val payload = mapOf("metadata" to this.metadata, "data" to this.data)
    val base64UrlHashedPayload = CryptoUtils.hash(payload)
    CryptoUtils.verify(detachedPayload = base64UrlHashedPayload, signature = this.signature)
  }

  /**
   * Uses [Json] to serialize the Message as a json string.
   *
   * @return The json string
   */
  override fun toString(): String {
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

      val message = jsonMapper.convertValue(messageJson, messageType)
      message.verify()

      return message
    }

  }
}



