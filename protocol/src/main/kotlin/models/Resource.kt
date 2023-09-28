package models

import net.pwall.json.schema.JSONSchema
import java.util.Date
import java.util.UUID


sealed class ResourceKind {
  class Offering : ResourceKind()
  class Reputation : ResourceKind()
}

abstract class Resource<T : ResourceKind> {
  abstract val metadata: ResourceMetadata // this could be out of sync with the type of data
  abstract val data: ResourceData
  abstract var signature: String?
}

class Offering(override val data: OfferingData, override val metadata: ResourceMetadata) : Resource<ResourceKind.Offering>() {
  override var signature: String? = null

  companion object {
    fun create(data: OfferingData): Offering {
      val kind = Resour.Offering // want this to be shared in the whole class and for implementers to have to provide it
      val metadata = ResourceMetadata(
        kind,
        "from",
        "${kind}_${UUID.randomUUID()}",
        Date()
      ) // dont like that the id logic lives here instead of on a base class
      return Offering(data, metadata)
    }
  }
}

//abstract class Resource<T: ResourceKind> {
//  abstract val metadata: ResourceMetadata<T> // this could be out of sync with the type of data
//  abstract val data: T
//  abstract var signature: String?
//}


class ReputationMetadata(
  override val from: String,
  override val updatedAt: Date?
) : ResourceMetadata(ResourceType.Reputation)

val metadata = OfferingMetadata("from", Date())

interface ResourceData<T>

class OfferingData(
  val description: String,
  val payoutUnitsPerPayinUnit: Int,
  val payoutCurrency: CurrencyDetails,
  val payinCurrency: CurrencyDetails,
  val payinMethods: List<PaymentMethod>,
  val payoutMethods: List<PaymentMethod>,
  val requiredClaims: PresentationExchange
) : ResourceData<Offering>


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

abstract class ResourceMetadata {
  abstract val from: String
  abstract val kind: String
  val id: String = "${kind}_${UUID.randomUUID()}"
  abstract val createdAt: Date // or kotlinx-datetime
  abstract val updatedAt: Date?

}

class Offering2 {
  private val metadata: OfferingMetadata
  private val data: OfferingData
  private var signature: String? = null

  private constructor(metadata: OfferingMetadata, data: OfferingData) {
    this.metadata = metadata
    this.data = data
  }

  companion object {
    fun create(from: String, id: String, data: OfferingData): Offering2 {
      val metadata = OfferingMetadata(from)
      return
    }
  }

  class OfferingMetadata(override val from: String, override val createdAt: Date, override val updatedAt: Date?) : ResourceMetadata() {
    override val kind: String = "offering"

  }
}

class Resource3<T>(val metadata: ResourceMetadata3<T>, // this could be out of sync with the type of data
                   val data: ResourceData<T>,
                   var signature: String?) {
  companion object {
    fun <T> parse(object: Any): Resource3<T> {

    }
  }
}

class ResourceMetadata3<T>(
  val from: String,
  val id: String,
  val createdAt: Date, // or kotlinx-datetime
  val updatedAt: Date? = null
) {
}

class ResourceData3<T>