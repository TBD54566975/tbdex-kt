package models

import java.time.Instant


interface ResourceMetadata {
  val from: String
  val id: String
  val createdAt: Instant
  val updatedAt: Instant?
  val kind: ResourceType
}

data class OfferingMetadata(
  override val from: String,
  override val id: String,
  override val createdAt: Instant,
  override val updatedAt: Instant?
) : ResourceMetadata {
  override val kind: ResourceType = ResourceType.Offering
}

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

