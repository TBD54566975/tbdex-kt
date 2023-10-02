package models

import net.pwall.json.schema.JSONSchema
import java.time.OffsetDateTime
import java.util.UUID


enum class ResourceKind {
  Offering,
  Reputation
}

class Resource<T : ResourceData>(
  val metadata: ResourceMetadata, // this could be out of sync with the type of data
  val data: T,
  var signature: String?
) {
  init {
    if (metadata.kind != data.kind) {
      throw IllegalArgumentException("Metadata and data types do not match.")
    }
  }
}

class ResourceMetadata(
  val from: String,
  val kind: ResourceKind,
  val id: String = "${kind}_${UUID.randomUUID()}",
  val createdAt: OffsetDateTime, // or kotlinx-datetime
  val updatedAt: OffsetDateTime?
)

sealed interface ResourceData {
  val kind: ResourceKind
}

class Offering(
  val description: String,
  val payoutUnitsPerPayinUnit: Int,
  val payoutCurrency: CurrencyDetails,
  val payinCurrency: CurrencyDetails,
  val payinMethods: List<PaymentMethod>,
  val payoutMethods: List<PaymentMethod>,
  val requiredClaims: PresentationExchange
) : ResourceData {
  override val kind = ResourceKind.Offering
}

class CurrencyDetails(
  val currencyCode: String,
  val minSubunits: String?,
  val maxSubunits: String?
)

class PaymentMethod(
  val kind: String,
  val requiredPaymentDetails: JSONSchema
)

class PresentationExchange

val offering = Resource(
  metadata = ResourceMetadata(
    from = "from",
    kind = ResourceKind.Offering,
    createdAt = OffsetDateTime.now(),
    updatedAt = OffsetDateTime.now()
  ),
  data = Offering(
    description = "",
    payoutUnitsPerPayinUnit = 1,
    payinCurrency = CurrencyDetails("", "", ""),
    payoutCurrency = CurrencyDetails("", "", ""),
    payinMethods = listOf(),
    payoutMethods = listOf(),
    requiredClaims = PresentationExchange()
  ),
  signature = null
)