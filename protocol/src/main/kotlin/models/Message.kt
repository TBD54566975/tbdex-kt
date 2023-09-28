package models

import java.util.Date
import kotlin.reflect.KClass

sealed class MessageType(val typeName: String) {
  object Rfq : MessageType("rfq")
  object Order : MessageType("order")
}

@Suppress("UNCHECKED_CAST")
abstract class Message<T : MessageType>(
  val metadata: MessageMetadata,
  val data: MessageData<T>,
  var signature: String? = null,
) {
  companion object {
    inline fun <reified T : MessageType> parse(message: Any): Message<T> {
      // verify message

      return when (T::class) {
        Rfq::class -> {
          return Rfq(RfqMetadata(), RfqData("blah", 1)) as Message<T> // requires suppress unchecked cast, any way around that?
        }

        Order::class -> {
          return Order(message) as Message<T>
        }

        else -> {}
      }
    }
  }
}

interface MessageData<T>
class RfqData(
  val offeringId: String,
  val payinSubunits: Int,
//  val payinMethod: SelectedPaymentMethod,
//  val payoutMethod: SelectedPaymentMethod,
//  val claims: List<String>
) : MessageData<MessageType.Rfq>

class SelectedPaymentMethod(val kind: String)
class OrderData : MessageData<MessageType.Order>

abstract class MessageMetadata(private val messageType: MessageType) {
  abstract val from: String
  abstract val to: String
  abstract val id: String
  abstract val exchangeId: String
  abstract val createdAt: Date

  val type: String
    get() = messageType.typeName
}

class RfqMetadata(override val from: String, override val to: String, override val id: String, override val exchangeId: String, override val createdAt: Date) : MessageMetadata(
  MessageType.Rfq
)

class OrderMetadata(override val from: String, override val to: String, override val id: String, override val exchangeId: String, override val createdAt: Date) : MessageMetadata(
  MessageType.Order
)

class Rfq(metadata: RfqMetadata, data: RfqData) : Message<MessageType.Rfq>(metadata, data)
class Order(metadata: OrderMetadata, data: OrderData) : Message<MessageType.Order>(metadata, data)

// should we make the constructor private and force people to go through a static Create() or Parse() method?
val rfq = Rfq(RfqMetadata(from = "from", to = "to", "id", "exchangeId", Date()), RfqData("offeringId"))
val test = Message.parse<MessageType.Rfq>(object { val test = "test"})

