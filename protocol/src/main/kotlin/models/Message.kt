package models

import com.fasterxml.jackson.annotation.JsonFormat
import dateTimeFormat
import typeid.TypeID
import java.time.OffsetDateTime

// TODO we don't really need this rn, but is one method of creating a common type that can be used in a hypothetical base class
sealed interface MessageData
class RfqData(val amount: Int) : MessageData
class OrderData : MessageData
class OrderStatusData: MessageData

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

abstract class Message<T: MessageData>(val data: T, val metadata: MessageMetadata, var signature: String? = null) {
  // TODO - use web5 crypto and fix the types
  fun sign(privateKey: String, kid: String){
    this.signature = "blah"
  }

  override fun toString(): String {
    return Mapper.writer().writeValueAsString(this)
  }

  init {
    when(metadata.kind) {
      MessageKind.rfq -> require(data is RfqData)
      MessageKind.order -> require(data is OrderData)
      MessageKind.orderstatus -> require(data is OrderStatusData)
    }
  }
}

// TODO add the other message types - haven't done it while we figure out the structure/interface we want



