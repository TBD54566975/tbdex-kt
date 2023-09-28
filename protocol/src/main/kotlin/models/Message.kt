package models

abstract class Message<T: MessageType>(
  val metadata: MessageMetadata,
  val data: MessageData<T>,
  var signature: String? = null,
)

interface MessageData<T>
class RfqData(val offeringId: String): MessageData<MessageType.Rfq>
class OrderData: MessageData<MessageType.Order>

sealed class MessageType(val typeName: String) {
  object Rfq : MessageType("rfq")
  object Order : MessageType("order")
}

abstract class MessageMetadata(private val messageType: MessageType) {
  abstract val from: String
  abstract val to: String

  val type: String
    get() = messageType.typeName
}
class RfqMetadata(override val from: String, override val to: String) : MessageMetadata(MessageType.Rfq)
class OrderMetadata(override val from: String, override val to: String) : MessageMetadata(MessageType.Order)

class Rfq(metadata: RfqMetadata, data: RfqData): Message<MessageType.Rfq>(metadata, data)
class Order(metadata: OrderMetadata, data: OrderData): Message<MessageType.Order>(metadata, data)

val rfq = Rfq(RfqMetadata("from", "to"), RfqData("offeringId"))
