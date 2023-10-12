package models

import CryptoUtils
import Json
import Json.jsonMapper
import Json.objectMapper
import StringToTypeIdDeserializer
import TypeIDToStringSerializer
import Validator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.readValue
import dateTimeFormat
import org.json.JSONObject
import typeid.TypeID
import web5.sdk.dids.Did
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
  @JsonSerialize(using = TypeIDToStringSerializer::class)
  @JsonDeserialize(using = StringToTypeIdDeserializer::class)
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

      validate(payload)
      return when (kindEnum) {
        ResourceKind.offering -> objectMapper.readValue<Offering>(payload)
//        ResourceKind.reputation -> TODO()
      }

      resource.verify()
      return resource
    }

    /**
     * Verifies the cryptographic integrity of the resource's signature.
     *
     * @throws Exception TODO link to crypto method throws
     */
    fun verify() {
      // TODO detached payload sig check (regenerate payload and then check)
    }

    /**
     * Validates a JSON payload based on tbDEX resource JSON schemas.
     *
     * @param payload The JSON payload to validate.
     * @throws Exception if the payload does not conform to the expected structure or data schema.
     */
    fun validate(payload: String) {
      val resourceJson = JSONObject(payload)
      
      // validate message structure
      Validator.validate(resourceJson, "resource")

      val dataJson = resourceJson.getJSONObject("data")
      val kind = resourceJson.getJSONObject("metadata").getString("kind")
      
      // validate specific resource data
      Validator.validate(dataJson, kind)
    }
  }
}
