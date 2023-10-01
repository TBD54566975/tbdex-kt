package models

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import typeid.TypeID
import java.time.OffsetDateTime
import java.util.Date

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

  abstract val kind: MessageType

  companion object {
    val mapper = jacksonObjectMapper()
    inline fun <reified T : MessageType> parse(message: String): Message<T> {
      // verify message

      mapper.readValue<Message<MessageType.Rfq>>(message)

      return when (T::class) {
        Rfq::class -> {
          return Rfq(
            RfqMetadata(message.),
            RfqData("blah", 1)
          ) as Message<T> // requires suppress unchecked cast, any way around that?
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
  abstract val id: TypeID
  abstract val exchangeId: String
  abstract val createdAt: OffsetDateTime

  val type: String
    get() = messageType.typeName

}

class RfqMetadata(override val from: String, override val to: String, override val id: TypeID, override val exchangeId: String, override val createdAt: OffsetDateTime) : MessageMetadata(
  MessageType.Rfq
)

class QuoteMetadata(override val from: String, override val to: String, override val id: TypeID, override val exchangeId: String, override val createdAt: OffsetDateTime) : MessageMetadata(
  MessageType.Quote
)

class OrderMetadata(override val from: String, override val to: String, override val id: TypeID, override val exchangeId: String, override val createdAt: OffsetDateTime) : MessageMetadata(
  MessageType.Order
)

class OrderStatusMetadata(override val from: String, override val to: String, override val id: TypeID, override val exchangeId: String, override val createdAt: OffsetDateTime) : MessageMetadata(
  MessageType.OrderStatus
)

class CloseMetadata(override val from: String, override val to: String, override val id: TypeID, override val exchangeId: String, override val createdAt: OffsetDateTime) : MessageMetadata(
  MessageType.Close
)

class RfqCreateArgs(
  val from: String,
  val to: String,
  val exchangeId: String,
  val offeringId: String,
  val payinSubunits: Int,
) {
  val kind = MessageType.Rfq
}

class Rfq(args: RfqCreateArgs) : Message<MessageType.Rfq>(
  RfqMetadata(
    args.from,
    args.to,
    TypeID(args.kind.typeName),
    args.exchangeId,
    OffsetDateTime.now()
  ), RfqData(args.offeringId, args.payinSubunits)
) {
  override val kind: MessageType = MessageType.Rfq

//    fun create(args: RfqCreateArgs): Message<MessageType.Rfq> {
//      val metadata = RfqMetadata(args.from, args.to, TypeID(this.), args.exchangeId, OffsetDateTime.now())
//      val data = RfqData(args.offeringId, args.payinSubunits)
//      return Rfq(metadata, data)
//    }


}

class Quote(metadata: QuoteMetadata, data: QuoteData) : Message<MessageType.Quote>(metadata, data)

class OrderCreateArgs(
  val from: String,
  val to: String,
  val exchangeId: String,
)

class Order(metadata: OrderMetadata, data: OrderData) : Message<MessageType.Order>(metadata, data) {
  override val kind: MessageType = MessageType.Order

  fun create(args: OrderCreateArgs): Message<MessageType.Order> {
    val metadata = OrderMetadata(args.from, args.to, TypeID(kind.typeName), args.exchangeId, OffsetDateTime.now())
    val data = OrderData()
    return Order(metadata, data)
  }
}

class OrderStatus(metadata: OrderStatusMetadata, data: OrderStatusData) : Message<MessageType.OrderStatus>(
  metadata,
  data
)

class Close(metadata: CloseMetadata, data: CloseData) : Message<MessageType.Close>(metadata, data)

// should we make the constructor private and force people to go through a static Create() or Parse() method?
val rfq = Rfq(RfqCreateArgs(from = "from", to = "to", "exchangeId","offeringId", 0))
val test = Message.parse<MessageType.Rfq>(object {
  val test = "test"
})

// using static create method
val rfq = Rfq

