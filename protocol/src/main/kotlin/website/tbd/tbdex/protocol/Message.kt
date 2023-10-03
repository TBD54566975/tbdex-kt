package website.tbd.tbdex.protocol

import com.fasterxml.jackson.annotation.JsonFormat
import typeid.TypeID
import java.time.OffsetDateTime
import website.tbd.tbdex.protocol.message_kinds.OrderData
import website.tbd.tbdex.protocol.message_kinds.OrderStatusData
import website.tbd.tbdex.protocol.message_kinds.RfqData

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

interface MessageData

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

    if (signature != null) {
      verify()
    } else {
      validate()
    }
  }

  private fun verify() {
    validate()

    // TODO detached payload sig check (regenerate payload and then check)
  }

  private fun validate() {
    // TODO validate against json schema
//    val schema = schemaMap.get(metadata.kind.name)
//    val jsonString = this.toString()
//    schema.validateBasic(jsonString)
//    if (output.errors != null) ...
  }

  // TODO - use web5 crypto and fix the types
  fun sign(privateKey: String, kid: String){
    this.signature = "blah"
  }

  override fun toString(): String {
    return Mapper.writer().writeValueAsString(this)
  }
}



