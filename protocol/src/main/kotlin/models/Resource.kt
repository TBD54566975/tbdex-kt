package models

import net.pwall.json.schema.JSONSchema
import java.time.Instant

enum class ResourceType {
  Offering,
  Reputation
}

abstract class Resource {
  abstract val metadata: ResourceMetadata
  abstract val data: ResourceData
  abstract val signature: String
}

interface ResourceData

data class OfferingData(
  val description: String,
  val payoutUnitsPerPayinUnit: Int,
  val payoutCurrency: CurrencyDetails,
  val payinCurrency: CurrencyDetails,
  val payinMethods: List<PaymentMethod>,
  val payoutMethods: List<PaymentMethod>,
  val requiredClaims: PresentationExchange
): ResourceData



data class CurrencyDetails(
  val currencyCode: String,
  val minSubunits: String?,
  val maxSubunits: String?
)

data class PaymentMethod(
  val kind: String,
  val requiredPaymentDetails: JSONSchema
)

data class Offering(
  override val metadata: OfferingMetadata,
  override val data: OfferingData,
  override val signature: String
): Resource()

val offering = Resource<Offering>(
  metadata = ResourceMetadata<Offering>("test", ),
  data = OfferingData("blah", 1),
  signature = "blah"
)