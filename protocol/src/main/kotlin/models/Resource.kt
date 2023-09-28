package models

import net.pwall.json.schema.JSONSchema
import java.util.Date
import java.util.UUID

enum class ResourceType {
  Offering,
  Reputation
}

sealed class Resource {
  abstract val metadata: ResourceMetadata // this could be out of sync with the type of data
  abstract val data: ResourceData
  abstract var signature: String?
}

class Offering(override val data: OfferingData, override val metadata: ResourceMetadata) : Resource() {
  override var signature: String? = null
  companion object {
    fun create(data: OfferingData): Offering {
      val kind = ResourceType.Offering // want this to be shared in the whole class and for implementers to have to provide it
      val metadata = ResourceMetadata(kind, "from", "${kind}_${UUID.randomUUID()}", Date()) // dont like that the id logic lives here instead of on a base class
      return Offering(data, metadata)
    }
  }
}

//abstract class Resource<T: ResourceKind> {
//  abstract val metadata: ResourceMetadata<T> // this could be out of sync with the type of data
//  abstract val data: T
//  abstract var signature: String?
//}

class ResourceMetadata(val kind: ResourceType,
                       val from: String,
                       val id: String,
                       val createdAt: Date, // or kotlinx-datetime
                       val updatedAt: Date? = null) {

}

//class OfferingMetadata(
//  override val from: String,
//  override val updatedAt: Date?
//) : ResourceMetadata(ResourceType.Offering) {
//  init {
//    super.id = "${kind}_${UUID.randomUUID()}"
//  }
//}



class ReputationMetadata(
  override val from: String,
  override val updatedAt: Date?
) : ResourceMetadata(ResourceType.Reputation)

val metadata = OfferingMetadata("from", Date())

interface ResourceData

class OfferingData(
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

//class Offering(
//  override val metadata: OfferingMetadata,
//  override val data: OfferingData,
//  override val signature: String
//) : Resource()

val offering = Resource<Offering>(
  metadata = ResourceMetadata<Offering>("test"),
  data = OfferingData("blah", 1),
  signature = "blah"
)