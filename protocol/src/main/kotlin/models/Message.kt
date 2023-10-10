package models

import CryptoUtils
import Json
import Json.jsonMapper
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.module.kotlin.readValue
import dateTimeFormat
import typeid.TypeID
import web5.sdk.dids.Did
import java.time.OffsetDateTime

/**
 * An enum representing all possible [Message] kinds.
 */
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
  val id: TypeID,
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

  init {
    // json schema validate
    validate()
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
    val base64UrlHashedPayload = CryptoUtils.hash(payload).toString()
    CryptoUtils.verify(detachedPayload = base64UrlHashedPayload, signature = this.signature)
  }

  /**
   * Validates the message against the corresponding json schema.
   *
   * @throws Exception if the message is invalid
   */
  private fun validate() {
    // TODO validate against json schema
//    val schema = schemaMap.get(metadata.kind.name)
//    val jsonString = this.toString()
//    schema.validateBasic(jsonString)
//    if (output.errors != null) ...
  }


  /**
   * Signs the Message using the specified [did] and optionally the given [keyAlias].
   *
   * @param did The DID (Decentralized Identifier) used for signing.
   * @param keyAlias The alias of the key to be used for signing (optional).
   * @throws Exception if the signing operation fails.
   */
  fun sign(did: Did, keyAlias: String? = null) {
    val payload = mapOf("metadata" to this.metadata, "data" to this.data)
    this.signature = CryptoUtils.sign(did = did, payload = payload, keyAlias = keyAlias)
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
      // TODO json schema validation using Message schema

      val node = Json.parse(payload)
      val kind = node.get("metadata").get("kind").asText()

      val kindEnum = MessageKind.valueOf(kind)

      val message = when (kindEnum) {
        MessageKind.rfq -> jsonMapper.readValue<Rfq>(payload)
        MessageKind.order -> jsonMapper.readValue<Order>(payload)
        MessageKind.orderstatus -> jsonMapper.readValue<OrderStatus>(payload)
        MessageKind.quote -> jsonMapper.readValue<Quote>(payload)
        MessageKind.close -> jsonMapper.readValue<Close>(payload)
      }

      message.verify()
      return message
    }
  }
}



