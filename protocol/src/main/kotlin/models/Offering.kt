package models

import models.Close.Companion.create
import models.Offering.Companion.create
import models.Order.Companion.create
import typeid.TypeID
import java.time.OffsetDateTime

/**
 * A class representing the Offering resource.
 * `Order` implements the [Resource] class and contains Offering specific data
 * - Create resource ([create])
 *
 * ### Example Usage:
 * ```kotlin
 * val offering = Offering.create(from, data)
 * ```
 */
class Offering private constructor(
  override val metadata: ResourceMetadata,
  override val data: OfferingData,
  override var signature: String? = null
) : Resource() {
  companion object {
    /**
     * Creates a new `Offering` resource, autopopulating the id, creation/updated time, and resource kind.
     *
     * @param from DID of the PFI.
     * @param data Specific parameters relevant to an Offering.
     * @return Offering instance.
     */
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
  }
}