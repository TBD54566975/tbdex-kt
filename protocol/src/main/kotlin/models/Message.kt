package models

// Abstract Message class with generics
sealed class MessageData
class RfqData(val amount: Int) : MessageData()
class OrderData : MessageData()
//
//sealed class MessageMetadata
//class RfqMetadata(): MessageMetadata()
//class OrderMetadata(): MessageMetadata()

// Abstract Message class with generics
//abstract class Message<TData : MessageData, TMetadata : Any> {
////  abstract val payload: TData // Abstract property
//  abstract companion object{
//  abstract fun create(data: TData): Message<TData, TMetadata>
//  abstract fun parse(data: Any): Message<TData, TMetadata>
//  }
//}

// Rfq class
class Rfq private constructor(private val rfqData: RfqData) {
  //  override val payload: IntPayload = IntPayload(rfqData)
  val metadata: MessageMetadata
  val data: RfqData
  var signature: String? = null

  fun sign() {
    this.signature = "blah"
  }

  companion object {
    fun create(data: RfqCreateArgs): Rfq {

    }

    fun parse(data: Any): Rfq {
      TODO("Not yet implemented")
    }
  }
}

class Order private constructor() {
  val metadata: MessageMetadata
  val data: OrderData
  var signature: String? = null

  companion object {
    fun create(): Order {
      return Order()
    }

    fun parse(data: Any): Order {
      TODO("Not yet implemented")
    }
  }
}

fun main() {
  val rfqMessage = Rfq.create(42)
  val orderMessage = Order.create("42.5")

  println("RFQ Payload: ${rfqMessage.data.value}")
  println("Quote Payload: ${orderMessage.data.value}")
}



