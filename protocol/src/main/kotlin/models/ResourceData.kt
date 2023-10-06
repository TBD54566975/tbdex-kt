package models

import net.pwall.json.schema.JSONSchema

/**
 * An interface that represents the contents of a [Resource]
 */
sealed interface ResourceData

/**
 * A data class implementing [ResourceData], which represents the contents of an [Offering]
 */
class OfferingData(
  val description: String,
  val payoutUnitsPerPayinUnit: Int,
  val payoutCurrency: CurrencyDetails,
  val payinCurrency: CurrencyDetails,
  val payinMethods: List<PaymentMethod>,
  val payoutMethods: List<PaymentMethod>,
  val requiredClaims: PresentationExchange
) : ResourceData

/**
 * A data class containing information pertaining to payin or payout
 */
class CurrencyDetails(
  val currencyCode: String,
  val minSubunits: Int? = null,
  val maxSubunits: Int? = null
)

/**
 * A data class containing information pertaining to payin or payout method
 */
class PaymentMethod(
  val kind: String,
  val requiredPaymentDetails: JSONSchema
)

/**
 * A data class containing information pertaining to verifiable credentials required to qualify for the offering
 */
class PresentationExchange