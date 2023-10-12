package models

import net.pwall.json.schema.JSONSchema
import web5.sdk.credentials.PresentationDefinitionV2

/**
 * An interface that represents the contents of a [Resource].
 */
sealed interface ResourceData

/**
 * A data class implementing [ResourceData], which represents the contents of an [Offering].
 */
class OfferingData(
  val description: String,
  val payoutUnitsPerPayinUnit: Int,
  val payoutCurrency: CurrencyDetails,
  val payinCurrency: CurrencyDetails,
  val payinMethods: List<PaymentMethod>,
  val payoutMethods: List<PaymentMethod>,
  val requiredClaims: List<PresentationDefinitionV2>
) : ResourceData

/**
 * A data class containing information pertaining to payin or payout.
 */
class CurrencyDetails(
  val currencyCode: String,
  val minSubunits: Int? = null,
  val maxSubunits: Int? = null
)

/**
 * A data class containing information pertaining to payin or payout method.
 */
class PaymentMethod(
  val kind: String,
  val requiredPaymentDetails: JSONSchema
)