package models

import net.pwall.json.schema.JSONSchema
import typeid.TypeID
import java.time.OffsetDateTime


enum class ResourceKind {
  offering,
  reputation
}

class ResourceMetadata(
  val kind: ResourceKind,
  val from: String,
  val id: TypeID,
  val createdAt: OffsetDateTime,
  val updatedAt: OffsetDateTime?
)

sealed interface ResourceData

class PresentationExchange

abstract class Resource<T : ResourceData>(
  val metadata: ResourceMetadata, // this could be out of sync with the type of data
  val data: T,
  var signature: String? = null
) {
  init {
    when (metadata.kind) {
      ResourceKind.offering -> require(data is OfferingData)
      ResourceKind.reputation -> TODO()
    }
  }

  fun sign() {
    this.signature = "blah"
  }

  override fun toString(): String {
    return Mapper.writer().writeValueAsString(this)
  }
}