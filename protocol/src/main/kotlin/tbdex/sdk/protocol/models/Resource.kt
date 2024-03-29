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
import web5.sdk.credentials.model.ConstraintsV2
import web5.sdk.credentials.model.FieldV2
import web5.sdk.credentials.model.InputDescriptorV2
import web5.sdk.credentials.model.PresentationDefinitionV2
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.methods.dht.DidDht
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
 * @property from The authors's DID
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
        ResourceKind.balance -> Balance::class.java
      }

      val resource = jsonMapper.convertValue(jsonResource, resourceType)
      resource.verify()

      return resource
    }
  }
}

// todo remove this once parse-offering.json is fixed
fun main() {
  val did = DidDht.create(InMemoryKeyManager())
  val offering = Offering.create(
    from = did.uri,
    data = OfferingData(
      description = "Selling BTC for USD",
      payoutUnitsPerPayinUnit = "0.00003826",
      payin = PayinDetails(
        currencyCode = "USD",
        min = "0.0",
        max = "999999.99",
        methods = listOf(
          PayinMethod(
            kind = "DEBIT_CARD",
            group = "card",
            min = "0.0",
            max = "999999.99",
            requiredPaymentDetails = jsonMapper.valueToTree(
              mapOf(
                "\$schema" to "http://json-schema.org/draft-07/schema",
                "type" to "object",
                "properties" to mapOf(
                  "cardNumber" to mapOf(
                    "type" to "string",
                    "description" to "The 16-digit debit card number",
                    "minLength" to 16,
                    "maxLength" to 16
                  ),
                  "expiryDate" to mapOf(
                    "type" to "string",
                    "description" to "The expiry date of the card in MM/YY format",
                    "pattern" to "^(0[1-9]|1[0-2])\\/([0-9]{2})$"
                  ),
                  "cardHolderName" to mapOf(
                    "type" to "string",
                    "description" to "Name of the cardholder as it appears on the card"
                  ),
                  "cvv" to mapOf(
                    "type" to "string",
                    "description" to "The 3-digit CVV code",
                    "minLength" to 3,
                    "maxLength" to 3
                  )
                ),
                "required" to listOf("cardNumber", "expiryDate", "cardHolderName", "cvv"),
                "additionalProperties" to false
              )
            )
          )
        )
      ),
      payout = PayoutDetails(
        currencyCode = "BTC",
        min = "999526.11",
        max = "99952611.00",
        methods = listOf(
          PayoutMethod(
            kind = "BTC_ADDRESS",
            group = "crypto",
            min = "999526.11",
            max = "99952611.00",
            estimatedSettlementTime = 3600,
            requiredPaymentDetails = jsonMapper.valueToTree(
              mapOf(
                "\$schema" to "http://json-schema.org/draft-07/schema",
                "type" to "object",
                "properties" to mapOf(
                  "btcAddress" to mapOf(
                    "type" to "string",
                    "description" to "your Bitcoin wallet address"
                  )
                ),
                "required" to listOf("btcAddress"),
                "additionalProperties" to false
              )
            )
          )
        )
      ),
      requiredClaims = PresentationDefinitionV2(
        id = "7ce4004c-3c38-4853-968b-e411bafcd945",
        inputDescriptors = listOf(
          InputDescriptorV2(
            id = "bbdb9b7c-5754-4f46-b63b-590bada959e0",
            constraints = ConstraintsV2(
              fields = listOf(
                FieldV2(
                  path = listOf("$.type"),
                  filterJson = jsonMapper.valueToTree(mapOf("type" to "string", "const" to "YoloCredential"))
                )
              )
            )
          )
        )
      )
    )
  )

  offering.sign(did)
  println(did.uri)
  println(offering.metadata.id)
  println(offering.signature)

  // eyJraWQiOiJkaWQ6ZGh0OmNwZjVyNmhxaHBiZ3pyZ3l0M3V3aGRmdGdmNWo4OTd3cmlmZ3ljZGg0ejF6NGprYnN0d3kjMCIsImFsZyI6IkVkRFNBIn0..JQJ5xExteH9PbSkzwa9QBHIBfbQ0PIyFRWJlJW77rFPOn9dKaxQ8uJNkN7Hz_VFSD2v5-bwgF2NeSm_DGoF_Aw
}

