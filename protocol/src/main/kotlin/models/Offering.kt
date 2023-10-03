package models

import net.pwall.json.schema.JSONSchema
import typeid.TypeID
import java.time.OffsetDateTime

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

class Offering private constructor(
  metadata: ResourceMetadata,
  data: OfferingData,
  signature: String? = null
): Resource<OfferingData>(metadata, data, signature) {
  companion object {
    fun create(from: String, data: OfferingData): Offering {
      val now = OffsetDateTime.now()
      val metadata = ResourceMetadata(
        kind = ResourceKind.offering,
        from = from,
        id = TypeID(ResourceKind.offering.name),
        createdAt = now,
        updatedAt = now
      )

      return Offering(metadata, data)
    }

    fun parse(data: String): Offering {
      // TODO verify the signature
      // TODO verify against json schemas

      // TODO not validated, do we need to read the subtypes individually? (metadata and data)
      return Mapper.reader(Offering::class.java).readValue(data)
    }
  }
}