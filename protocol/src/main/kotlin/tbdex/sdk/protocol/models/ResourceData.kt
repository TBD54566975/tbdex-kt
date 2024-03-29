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
 *
 * @property description Brief description of what is being offered
 * @property payoutUnitsPerPayinUnit Number of payout units alice would get for 1 payin unit
 * @property payin Details and options associated to the payin currency
 * @property payout Details and options associated to the payout currency
 * @property requiredClaims Claim(s) required when submitting an RFQ for this offering
 */
class OfferingData(
  val description: String,
  val payoutUnitsPerPayinUnit: String,
  val payin: PayinDetails,
  val payout: PayoutDetails,
  val requiredClaims: PresentationDefinitionV2?
) : ResourceData

/**
 * A data class representing the structure and common functionality available on PayinDetails and PayoutDetails.
 *
 * @property currencyCode ISO 3166 currency code string.
 * @property min Minimum amount of currency that the offer is valid for.
 * @property max Minimum amount of currency that the offer is valid for.
 * @property methods A list of payment methods to select from.
 */
sealed class PaymentDetails(
  val currencyCode: String,
  val min: String? = null,
  val max: String? = null,
  val methods: List<PaymentMethod>
)

/**
 * A data class containing information pertaining to payin.
 *
 * @param currencyCode ISO 3166 currency code string
 * @param min Minimum amount of currency that the offer is valid for
 * @param max Maximum amount of currency that the offer is valid for
 * @param methods A list of payin methods to select from
 */
class PayinDetails(
  currencyCode: String,
  min: String? = null,
  max: String? = null,
  methods: List<PayinMethod>
) : PaymentDetails(currencyCode, min, max, methods)

/**
 * A data class containing information pertaining to payout.
 *
 * @param currencyCode ISO 3166 currency code string
 * @param min Minimum amount of currency that the offer is valid for
 * @param max Maximum amount of currency that the offer is valid for
 * @param methods A list of payout methods to select from
 */
class PayoutDetails(
  currencyCode: String,
  min: String? = null,
  max: String? = null,
  methods: List<PayoutMethod>
) : PaymentDetails(currencyCode, min, max, methods)

/**
 * An abstract class representing the structure and common functionality
 * available on all PaymentMethods.
 *
 * @property kind Unique string identifying a single kind of payment method.
 * @property name Payment Method name. Expected to be rendered on screen.
 * @property description Blurb containing helpful information about the payment method.
 *                       Expected to be rendered on screen. e.g. "segwit addresses only"
 * @property group Value that can be used to group specific payment methods together
 *                       e.g. Mobile Money vs. Direct Bank Deposit
 * @property requiredPaymentDetails A JSON Schema containing the fields that need to be collected
 *                                  in the RFQ's selected payment methods in order to use this payment method.
 * @property fee Fee charged to use this payment method. Absence of this field implies that
 *               there is no additional fee associated to the respective payment method.
 * @property min Minimum amount required to use this payment method.
 * @property max Maximum amount allowed when using this payment method.
 */
sealed class PaymentMethod(
  val kind: String,
  val name: String? = null,
  val description: String? = null,
  val group: String? = null,
  val requiredPaymentDetails: JsonNode? = null,
  val fee: String? = null,
  val min: String? = null,
  val max: String? = null
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

/**
 * A data class containing information pertaining to payin method.
 *
 * @property kind Unique string identifying a single kind of payment method.
 * @property name Payment Method name. Expected to be rendered on screen.
 * @property description Blurb containing helpful information about the payment method.
 *                       Expected to be rendered on screen. e.g. "segwit addresses only"
 * @property group Value that can be used to group specific payment methods together
 *                       e.g. Mobile Money vs. Direct Bank Deposit
 * @property requiredPaymentDetails A JSON Schema containing the fields that need to be collected
 *                                  in the RFQ's selected payment methods in order to use this payment method.
 * @property fee Fee charged to use this payment method. Absence of this field implies that
 *               there is no additional fee associated to the respective payment method.
 * @property min Minimum amount required to use this payment method.
 * @property max Maximum amount allowed when using this payment method.
 */
class PayinMethod(
  kind: String,
  name: String? = null,
  description: String? = null,
  group: String? = null,
  requiredPaymentDetails: JsonNode? = null,
  fee: String? = null,
  min: String? = null,
  max: String? = null
) : PaymentMethod(kind, name, description, group, requiredPaymentDetails, fee, min, max)

/**
 * A data class containing information pertaining to payout method.
 *
 * @property kind Unique string identifying a single kind of payment method.
 * @property name Payment Method name. Expected to be rendered on screen.
 * @property description Blurb containing helpful information about the payment method.
 *                       Expected to be rendered on screen. e.g. "segwit addresses only"
 * @property group Value that can be used to group specific payment methods together
 *                       e.g. Mobile Money vs. Direct Bank Deposit
 * @property requiredPaymentDetails A JSON Schema containing the fields that need to be collected
 *                                  in the RFQ's selected payment methods in order to use this payment method.
 * @property fee Fee charged to use this payment method. Absence of this field implies that
 *               there is no additional fee associated to the respective payment method.
 * @property min Minimum amount required to use this payment method.
 * @property max Maximum amount allowed when using this payment method.
 * @property estimatedSettlementTime Estimated time taken to settle an order. Expressed in seconds.
 */
class PayoutMethod(
  kind: String,
  name: String? = null,
  description: String? = null,
  group: String? = null,
  requiredPaymentDetails: JsonNode? = null,
  fee: String? = null,
  min: String? = null,
  max: String? = null,
  val estimatedSettlementTime: Int
) : PaymentMethod(kind, name, description, group, requiredPaymentDetails, fee, min, max)


/**
 * A data class implementing [ResourceData], which represents the contents of a [Balance].
 *
 * @property currencyCode ISO 3166 currency code string
 * @property amount The amount available to be transacted with
 * @constructor Create empty Balance data
 */
class BalanceData(
  val currencyCode: String,
  val amount: String
) : ResourceData
