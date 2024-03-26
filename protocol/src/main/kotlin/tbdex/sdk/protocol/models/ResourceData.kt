package tbdex.sdk.protocol.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import web5.sdk.credentials.model.PresentationDefinitionV2

/**
 * An interface that represents the data content of any tbDEX object.
 */
sealed interface Data

/**
 * An interface that represents the contents of a [Resource].
 */
sealed interface ResourceData : Data

/**
 * A data class implementing [ResourceData], which represents the contents of an [Offering].
 */
class OfferingData(
  val description: String,
  val payoutUnitsPerPayinUnit: String,
  val payout: PaymentDetails,
  val payin: PaymentDetails,
  val requiredClaims: PresentationDefinitionV2?
) : ResourceData

/**
 * A data class containing information pertaining to payin or payout.
 */
class PaymentDetails(
  val currencyCode: String,
  val methods: List<PaymentMethod>,
  val min: String? = null,
  val max: String? = null,
)

/**
 * A data class containing information pertaining to payin or payout method.
 */
class PaymentMethod(
  val kind: String,
  val requiredPaymentDetails: JsonNode? = null,
  val min: String? = null,
  val max: String? = null,
  val description: String? = null,
  val name: String? = null,
  val group: String? = null,
) {
  /**
   * Parse the contents of [requiredPaymentDetails] into a [JsonSchema] that can do validation.
   */
  @JsonIgnore
  fun getRequiredPaymentDetailsSchema(): JsonSchema? {
    if (requiredPaymentDetails == null) return null
    val schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
    return schemaFactory.getSchema(requiredPaymentDetails)
  }
}

