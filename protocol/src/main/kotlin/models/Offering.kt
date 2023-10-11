package models

import typeid.TypeID
import java.time.OffsetDateTime

class Offering private constructor(
  override val metadata: ResourceMetadata,
  override val data: OfferingData,
  override var signature: String? = null
) : Resource() {
  companion object {
    fun create(from: String, data: OfferingData): Offering {
      val now = OffsetDateTime.now()
      val metadata = ResourceMetadata(
        kind = ResourceKind.offering,
        from = from,
        id = TypeID(ResourceKind.offering.name).toString(),
        createdAt = now,
        updatedAt = now
      )

      return Offering(metadata, data)
    }
  }
}