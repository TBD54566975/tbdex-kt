package models.playground

import models.CurrencyDetails
import models.PaymentMethod

class Resource<T : ResourceKind>(
  val metadata: ResourceMetadata<T>,// this could be out of sync with the type of data
  val data: ResourceData<T>,
  var signature: String?
)

class ResourceMetadata<T: ResourceKind>(
  val from: String,
  val kind: T,
  val id: String,
  val createdAt: String,
  val updatedAt: String? = null
)

class ResourceModel<T : ResourceKind>(
  val metadata: ResourceMetadata<T>,
  val data: ResourceKindModel<T>,
  val signature: String
)

interface ResourceData<T>

sealed class ResourceKind {
  data object Offering : ResourceKind()
  data object Reputation : ResourceKind()
}

class OfferingData(
  val description: String,
  val payoutUnitsPerPayinUnit: Int,
  val payoutCurrency: CurrencyDetails,
  val payinCurrency: CurrencyDetails,
  val payinMethods: List<PaymentMethod>,
  val payoutMethods: List<PaymentMethod>,
  val requiredClaims: PresentationExchange
) : ResourceKinds()

class PresentationExchange {
}

typealias ResourceKindModel<T> = ResourceKinds

sealed class ResourceKinds

val offeringResource : Resource<ResourceKind.Offering> {

}
