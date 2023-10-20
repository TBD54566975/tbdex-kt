package tbdex.sdk.protocol.models

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import org.json.JSONObject
import tbdex.sdk.protocol.CryptoUtils
import tbdex.sdk.protocol.Json
import tbdex.sdk.protocol.Json.jsonMapper
import tbdex.sdk.protocol.Validator
import tbdex.sdk.protocol.dateTimeFormat
import typeid.TypeID
import web5.sdk.dids.Did
import java.lang.IllegalArgumentException
import java.time.OffsetDateTime

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
      val jsonResource = try {
        jsonMapper.readTree(payload)
      } catch(e: JsonParseException) {
        throw IllegalArgumentException("unexpected character at offset ${e.location.charOffset}")
      }

      require(jsonResource.isObject) { "expected payload to be a json object" }

      // validate message structure
      Validator.validate(jsonResource, "resource")

      val dataJson = jsonResource.get("data")
      val kind = jsonResource.get("metadata").get("kind").asText()

      // validate specific resource data
      Validator.validate(dataJson, kind)

      val resourceType =  when (ResourceKind.valueOf(kind)) {
        ResourceKind.offering -> Offering::class.java
        // ResourceKind.reputation -> TODO()
      }

      val resource = jsonMapper.convertValue(jsonResource, resourceType)
      resource.verify()

      return resource
    }
  }
}
