package tbdex.sdk.protocol.models

import com.fasterxml.jackson.annotation.JsonFormat
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
 * An interface that represents the metadata of a tbDEX object.
 */
sealed interface Metadata

/**
 * An enum representing all possible [Resource] kinds.
 */
enum class ResourceKind {
  offering,
  balance
}

/**
 * A data class representing the metadata present on every [Resource].
 *
 * @property kind the data property's type. e.g. offering
 * @property from The author's DID
 * @property id The resource's ID
 * @property protocol Version of the protocol in use (x.x format).
 *                    The protocol version must remain consistent across messages in a given exchange.
 *                    Messages sharing the same exchangeId MUST also have the same protocol version.
 *                    Protocol versions are tracked in https://github.com/TBD54566975/tbdex
 * @property createdAt ISO 8601 timestamp
 * @property updatedAt ISO 8601 timestamp
 */
class ResourceMetadata(
  val kind: ResourceKind,
  val from: String,
  val id: String,
  val protocol: String,
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimeFormat, timezone = "UTC")
  val createdAt: OffsetDateTime,
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimeFormat, timezone = "UTC")
  val updatedAt: OffsetDateTime?
) : Metadata

/**
 * An abstract class representing the structure and common functionality available on all Resources.
 */
sealed class Resource {
  abstract val metadata: ResourceMetadata
  abstract val data: ResourceData
  abstract var signature: String?

  /**
   * Signs the Resource using the specified [BearerDid]
   *
   * @param did The DID (Decentralized Identifier) used for signing.
   * @throws Exception if the signing operation fails.
   */
  fun sign(did: BearerDid) {
    this.signature = Jws.sign(bearerDid = did, payload = this.digest(), detached = true)
  }

  /**
   * Verifies the signature of the Resource.
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
  private fun digest(): ByteArray = SignatureVerifier.digestOf(metadata, data)

  /**
   * Uses [Json] to serialize the Resource as a json string.
   *
   * @return The json string
   */
  override fun toString(): String {
    return Json.stringify(this)
  }
}
