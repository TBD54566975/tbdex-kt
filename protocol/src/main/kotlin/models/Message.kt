package models

import com.fasterxml.jackson.annotation.JsonFormat
import dateTimeFormat
import typeid.TypeID
import java.time.OffsetDateTime

// TODO: linter gonna yell at us for this, but I want the typeid and serialization to be ez for now
enum class MessageKind {
  rfq, order, orderstatus
}
class MessageMetadata(
  val kind: MessageKind,
  val to: String,
  val from: String,
  val id: TypeID,
  val exchangeId: TypeID,
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimeFormat, timezone = "UTC")
  val createdAt: OffsetDateTime
)

sealed interface MessageData

abstract class Message<T: MessageData>(
  val metadata: MessageMetadata,
  val data: T,
  var signature: String? = null
) {
  init {
    when(metadata.kind) {
      MessageKind.rfq -> require(data is RfqData)
      MessageKind.order -> require(data is OrderData)
      MessageKind.orderstatus -> require(data is OrderStatusData)
    }
  }

  // TODO - use web5 crypto and fix the types
  fun sign(privateKey: String, kid: String){
    this.signature = "blah"
  }

  override fun toString(): String {
    return Mapper.writer().writeValueAsString(this)
  }
}



