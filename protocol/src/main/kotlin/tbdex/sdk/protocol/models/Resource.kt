package tbdex.sdk.protocol.models

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import tbdex.sdk.protocol.CryptoUtils
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
  offering
}

/**
 * A data class representing the metadata present on every [Resource].
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
    CryptoUtils.verify(detachedPayload = digest(), signature = signature, did = metadata.from)
  }

  /**
   * Generates a digest of the message for signing or verification.
   *
   * @return The message digest as a byte array.
   */
  private fun digest(): ByteArray = CryptoUtils.digestOf(metadata, data)

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
      val jsonResource: JsonNode = try {
        jsonMapper.readTree(payload)
      } catch (e: JsonParseException) {
        throw IllegalArgumentException("unexpected character at offset ${e.location.charOffset}")
      }

      require(jsonResource.isObject) { "expected payload to be a json object" }

      // validate message structure
      Validator.validate(jsonResource, "resource")

      val dataJson = jsonResource.get("data")
      val kind = jsonResource.get("metadata").get("kind").asText()

      // validate specific resource data
      Validator.validate(dataJson, kind)

      val resourceType = when (ResourceKind.valueOf(kind)) {
        ResourceKind.offering -> Offering::class.java
        // ResourceKind.reputation -> TODO()
      }

      val resource = jsonMapper.convertValue(jsonResource, resourceType)
      resource.verify()

      return resource
    }
  }
}
