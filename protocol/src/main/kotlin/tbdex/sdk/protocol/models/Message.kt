package tbdex.sdk.protocol.models

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import tbdex.sdk.protocol.SignatureVerifier
import tbdex.sdk.protocol.Validator
import tbdex.sdk.protocol.serialization.Json
import tbdex.sdk.protocol.serialization.Json.jsonMapper
import tbdex.sdk.protocol.serialization.dateTimeFormat
import web5.sdk.dids.did.BearerDid
import web5.sdk.jose.jws.Jws
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
  val id: String,
  val exchangeId: String,
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimeFormat, timezone = "UTC")
  val createdAt: OffsetDateTime,
  val protocol: String,
  val externalId: String? = null
) : Metadata

/**
 * An abstract class representing the structure and common functionality available on all Messages.
 */
@JsonIgnoreProperties(value = ["validNext"])
sealed class Message {
  abstract val validNext: Set<MessageKind>
  abstract val metadata: MessageMetadata
  abstract val data: MessageData
  abstract var signature: String?

  /**
   * Signs the Message using the specified [BearerDid].
   *
   * @param did The DID (Decentralized Identifier) used for signing.
   * @throws Exception if the signing operation fails.
   */
  fun sign(did: BearerDid) {
    this.signature = Jws.sign(bearerDid = did, payload = this.digest(), detached = true)
  }

  /**
   * Verifies the signature of the data.
   *
   * This function verifies the signature using the previously set [signature] property.
   * It compares the signature against a hashed payload consisting of metadata and data.
   *
   * @throws Exception if the verification fails or if the signature is missing.
   */
  fun verify() {
    SignatureVerifier.verify(detachedPayload = digest(), signature = signature, did = metadata.from)
  }

  /**
   * Generates a digest of the message for signing or verification.
   *
   * @return The message digest as a byte array.
   */
  private fun digest(): ByteArray = SignatureVerifier.digestOf(this.metadata, this.data)

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
      val jsonMessage: JsonNode

      try {
        jsonMessage = jsonMapper.readTree(payload)
      } catch (e: JsonParseException) {
        throw IllegalArgumentException("unexpected character at offset ${e.location.charOffset}")
      }

      require(jsonMessage.isObject) { "expected payload to be a json object" }

      // validate message structure
      Validator.validate(jsonMessage, "message")

      val jsonMessageData = jsonMessage.get("data")
      val kind = jsonMessage.get("metadata").get("kind").asText()

      // validate specific message data (Rfq, Quote, etc)
      Validator.validate(jsonMessageData, kind)

      val messageType = when (MessageKind.valueOf(kind)) {
        MessageKind.rfq -> Rfq::class.java
        MessageKind.order -> Order::class.java
        MessageKind.orderstatus -> OrderStatus::class.java
        MessageKind.quote -> Quote::class.java
        MessageKind.close -> Close::class.java
      }

      val message = jsonMapper.convertValue(jsonMessage, messageType)
      message.verify()

      return message
    }
  }
}



