package models.playground

import models.CurrencyDetails
import models.PaymentMethod
import models.ResourceData
import models.ResourceKind

abstract class Resource<T : ResourceKind> {
  abstract val metadata: ResourceMetadata<T>// this could be out of sync with the type of data
  abstract val data: ResourceData<T>
  abstract var signature: String?
}

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

