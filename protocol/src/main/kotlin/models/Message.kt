package models

import java.util.Date
import kotlin.reflect.KClass

sealed class MessageType(val typeName: String) {
  object Rfq : MessageType("rfq")
  object Quote : MessageType("quote")
  object Order : MessageType("order")
  object OrderStatus : MessageType("orderstatus") // use camel case or kebab case?
  object Close : MessageType("close")
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

        Quote::class -> {
          return Quote(QuoteMetadata(), QuoteData(expiresAt = Date())) as Message<T>
        }

        Order::class -> {
          return Order(OrderMetadata(), OrderData()) as Message<T>
        }

        OrderStatus::class -> {
          return OrderStatus(OrderStatusMetadata(), OrderStatusData("bleh")) as Message<T>
        }

        Close::class -> {
          return Close(CloseMetadata(), CloseData("bleh")) as Message<T>
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

class QuoteData(
  val expiresAt: Date,
//  val payin: QuoteDetails,
//  val payout: QuoteDetails,
//  val paymentInstructions: PaymentInstructions?,
) : MessageData<MessageType.Quote>

class QuoteDetails(val currencyCode: String, val amountSubunits: String, val feeSubunits: String?)

class PaymentInstructions(val payin: PaymentInstruction?, val payout: PaymentInstruction?)

class PaymentInstruction(val link: String?, val instruction: String?)


class OrderData : MessageData<MessageType.Order>

class OrderStatusData(
  val orderStatus: String,
) : MessageData<MessageType.OrderStatus>

class CloseData(
  val reason: String?,
) : MessageData<MessageType.Close>

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

class QuoteMetadata(override val from: String, override val to: String, override val id: String, override val exchangeId: String, override val createdAt: Date) : MessageMetadata(
  MessageType.Quote
)

class OrderMetadata(override val from: String, override val to: String, override val id: String, override val exchangeId: String, override val createdAt: Date) : MessageMetadata(
  MessageType.Order
)

class OrderStatusMetadata(override val from: String, override val to: String, override val id: String, override val exchangeId: String, override val createdAt: Date) : MessageMetadata(
  MessageType.OrderStatus
)

class CloseMetadata(override val from: String, override val to: String, override val id: String, override val exchangeId: String, override val createdAt: Date) : MessageMetadata(
  MessageType.Close
)

class Rfq(metadata: RfqMetadata, data: RfqData) : Message<MessageType.Rfq>(metadata, data)
class Quote(metadata: QuoteMetadata, data: QuoteData) : Message<MessageType.Quote>(metadata, data)
class Order(metadata: OrderMetadata, data: OrderData) : Message<MessageType.Order>(metadata, data)
class OrderStatus(metadata: OrderStatusMetadata, data: OrderStatusData) : Message<MessageType.OrderStatus>(metadata, data)
class Close(metadata: CloseMetadata, data: CloseData) : Message<MessageType.Close>(metadata, data)

// should we make the constructor private and force people to go through a static Create() or Parse() method?
val rfq = Rfq(RfqMetadata(from = "from", to = "to", "id", "exchangeId", Date()), RfqData("offeringId"))
val test = Message.parse<MessageType.Rfq>(object { val test = "test"})

