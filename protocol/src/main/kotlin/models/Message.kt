package models
import java.util.Date

class Message<T: MessageData>(
  val metadata: MessageMetadata,
  val data: T,
  var signature: String? = null,
) {
  init {
    if (metadata.messageType != data.getMessageType()) {
      throw IllegalArgumentException("Metadata and data types do not match.")
    }
  }
}

interface MessageData {
  fun getMessageType(): MessageType
}
class RfqData(val offeringId: String): MessageData {
  override fun getMessageType(): MessageType = MessageType.Rfq
}
class QuoteData(val expiresAt: Date): MessageData {
  override fun getMessageType(): MessageType = MessageType.Quote
}
class OrderData: MessageData {
  override fun getMessageType(): MessageType = MessageType.Order
}
class OrderStatusData(val orderStatus: String): MessageData {
  override fun getMessageType(): MessageType = MessageType.OrderStatus
}
class CloseData(val reason: String?): MessageData {
  override fun getMessageType(): MessageType = MessageType.Close
}

enum class MessageType {
  Rfq,
  Quote,
  Order,
  OrderStatus,
  Close
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
