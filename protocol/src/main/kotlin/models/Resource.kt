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
 * An enum representing all possible [Resource] kinds.
 */
enum class ResourceKind {
  offering,
  reputation
}

/**
 * A data class representing the metadata present on every [Resource].
 */
class ResourceMetadata(
  val kind: ResourceKind,
  val from: String,
  val id: TypeID,
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimeFormat, timezone = "UTC")
  val createdAt: OffsetDateTime,
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimeFormat, timezone = "UTC")
  val updatedAt: OffsetDateTime?
)

/**
 * An abstract class representing the structure and common functionality available on all Resources.
 */
sealed class Resource {
  abstract val metadata: ResourceMetadata
  abstract val data: ResourceData
  abstract var signature: String?

  init {
    // json schema validate
    validate()
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
    val payload = mapOf("metadata" to this.metadata, "data" to this.data)
    val base64UrlHashedPayload = CryptoUtils.hash(payload)
    CryptoUtils.verify(detachedPayload = base64UrlHashedPayload, signature = this.signature)
  }

  /**
   * Validates the resource against the corresponding json schema.
   *
   * @throws Exception if the resource is invalid
   */
  private fun validate() {
    // TODO validate against json schema
//    val schema = schemaMap.get(metadata.kind.name)
//    val jsonString = this.toString()
//    schema.validateBasic(jsonString)
//    if (output.errors != null) ...
  }

  /**
   * Signs the Resource using the specified [did] and optionally the given [keyAlias].
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
   * Uses [Json] to serialize the Resource as a json string.
   *
   * @return The json string
   */
  override fun toString(): String {
    return Json.stringify(this)
  }

  companion object {
    /**
     * Takes an existing Resource in the form of a json string and parses it into a Resource object.
     * Validates object structure and performs an integrity check using the resource signature.
     *
     * @param payload The resource as a json string.
     * @return The json string parsed into a concrete Resource implementation.
     * @throws IllegalArgumentException if the payload is not valid json.
     * @throws IllegalArgumentException if the payload does not conform to the expected json schema.
     * @throws IllegalArgumentException if the payload signature verification fails.
     */
    fun parse(payload: String): Resource {
      // TODO json schema validation using Resource schema

      val node = Json.parse(payload)
      val kind = node.get("metadata").get("kind").asText()

      val kindEnum = ResourceKind.valueOf(kind)

      // TODO json schema validation using specific type schema

      val resource = when (kindEnum) {
        ResourceKind.offering -> jsonMapper.readValue<Offering>(payload)
        ResourceKind.reputation -> throw NotImplementedError()
      }

      resource.verify()
      return resource
    }
  }
}