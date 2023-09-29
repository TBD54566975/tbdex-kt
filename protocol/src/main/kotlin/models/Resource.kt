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
)

class ResourceMetadata(
  val from: String,
  val kind: ResourceKind,
  val id: String = "${kind}_${UUID.randomUUID()}",
  val createdAt: OffsetDateTime, // or kotlinx-datetime
  val updatedAt: OffsetDateTime?
)

sealed interface ResourceData

class Offering(
  val description: String,
  val payoutUnitsPerPayinUnit: Int,
  val payoutCurrency: CurrencyDetails,
  val payinCurrency: CurrencyDetails,
  val payinMethods: List<PaymentMethod>,
  val payoutMethods: List<PaymentMethod>,
  val requiredClaims: PresentationExchange
) : ResourceData

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

//class Offering2 {
//  private val metadata: OfferingMetadata
//  private val data: OfferingData
//  private var signature: String? = null
//
//  private constructor(metadata: OfferingMetadata, data: OfferingData) {
//    this.metadata = metadata
//    this.data = data
//  }
//
//  companion object {
//    fun create(from: String, id: String, data: OfferingData): Offering2 {
//      val metadata = OfferingMetadata(from)
//      return
//    }
//  }
//
//  class OfferingMetadata(override val from: String, override val createdAt: Date, override val updatedAt: Date?) : ResourceMetadata() {
//    override val kind: String = "offering"
//
//  }
//}
//
//class Resource3<T>(val metadata: ResourceMetadata3<T>, // this could be out of sync with the type of data
//                   val data: ResourceData<T>,
//                   var signature: String?) {
//  companion object {
//    fun <T> parse(object: Any): Resource3<T> {
//
//    }
//  }
//}
//
//class ResourceMetadata3<T>(
//  val from: String,
//  val id: String,
//  val createdAt: Date, // or kotlinx-datetime
//  val updatedAt: Date? = null
//) {
//}
//
//class ResourceData3<T>