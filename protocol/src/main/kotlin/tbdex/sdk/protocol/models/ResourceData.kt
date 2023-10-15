package tbdex.sdk.protocol.models

import org.everit.json.schema.Schema
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
  val payoutUnitsPerPayinUnit: String,
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
  val minSubunits: String? = null,
  val maxSubunits: String? = null
)

/**
 * A data class containing information pertaining to payin or payout method.
 */
class PaymentMethod(
  val kind: String,
  val requiredPaymentDetails: Schema? = null
)
