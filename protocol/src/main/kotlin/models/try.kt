package models

import java.time.Instant
import java.util.Date
import java.util.UUID


//abstract class ResourceMetadata(val kind: ResourceType) {
//  abstract val from: String
//  val id: String
//  val createdAt: Date = Date() // or kotlinx-datetime
//  abstract val updatedAt: Date?
//
//  init {
//    this.id = "${kind}_${UUID.randomUUID()}"
//  }
//}
//
//class OfferingMetadata(
//  override val from: String,
//  override val updatedAt: Date?
//) : ResourceMetadata(ResourceType.Offering)
//
//class ReputationMetadata(
//  override val from: String,
//  override val updatedAt: Date?
//) : ResourceMetadata(ResourceType.Reputation)
//
//val metadata = OfferingMetadata("from", Date()).kind


class ResourceMetadata2(
  val from: String,
  val id: String,
  val createdAt: Instant,
  val updatedAt: Instant?,
  val kind: String
) {
  constructor(
    from: String,
    id: String,
    createdAt: Instant,
    updatedAt: Instant?
  ) : this(from, id, createdAt, updatedAt, mapTypeParameterToKind<String>())

  companion object {
    // Define a mapping from type parameter T to kind value
    private inline fun <reified T : String> mapTypeParameterToKind(): String {
      return when (T::class.simpleName) {
        "Offering" -> "Offering"
        "Reputation" -> "Reputation"
        else -> throw IllegalArgumentException("Invalid type parameter: ${T::class.simpleName}")
      }
    }
  }
}

