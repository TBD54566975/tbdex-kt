package models

class Message<T: MessageData>(
  val metadata: MessageMetadata,
  val data: T,
  var signature: String? = null,
)

interface MessageData
class RfqData(val offeringId: String): MessageData
class OrderData: MessageData

enum class MessageType {
  Rfq,
  Order
}

class MessageMetadata(
  val messageType: MessageType,
  val from: String,
  val to: String,
)

val rfq = Message<RfqData>(
  MessageMetadata(MessageType.Rfq,
    "from",
    "to"
  ),
  RfqData("offeringId")
)
