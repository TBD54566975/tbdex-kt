package models

import java.time.OffsetDateTime

// TODO we don't really need this rn, but is one method of creating a common type that can be used in a hypothetical base class
sealed class MessageData
class RfqData(val amount: Int) : MessageData()
class OrderData : MessageData()

sealed interface MessageKind{
  val name: String
}
object RfqKind: MessageKind {
  override val name: String = "rfq"
}
object OrderKind: MessageKind {
  override val name: String = "order"
}

class MessageMetadata(
  val kind: MessageKind,
  val to: String,
  val from: String,
  val createdAt: OffsetDateTime,
  val updatedAt: OffsetDateTime
)

class Rfq private constructor(val data: RfqData, val metadata: MessageMetadata) {
  // TODO signature and sign seem ripe to be abstracted into a base class
  var signature: String? = null

  // TODO - use web5 crypto and fix the types
  fun sign(privateKey: String, kid: String) {
    this.signature = "blah"
  }

  companion object {
    fun create(to: String, from: String, amount: Int): Rfq {
      val metadata = MessageMetadata(
        kind = RfqKind,
        to = to,
        from = from,
        createdAt = OffsetDateTime.now(),
        updatedAt = OffsetDateTime.now()
      )

      val data = RfqData(amount)
      return Rfq(data, metadata)
    }

    fun parse(data: String): Rfq {
      // TODO verify the signature
      // TODO verify against json schemas
      // TODO - pull in a json serlialization lib and parse into Rfq class
      TODO("Not yet implemented")
    }
  }
}

class Order private constructor(val data: OrderData, val metadata: MessageMetadata) {
  var signature: String? = null

  // TODO - use web5 crypto and fix the types
  fun sign(privateKey: String, kid: String) {
    this.signature = "blah"
  }

  companion object {
    fun create(to: String, from: String): Order {
      val metadata = MessageMetadata(
        kind = OrderKind,
        to = to,
        from = from,
        createdAt = OffsetDateTime.now(),
        updatedAt = OffsetDateTime.now()
      )
      return Order(OrderData(), metadata)
    }

    fun parse(data: Any): Order {
      // TODO verify the signature
      // TODO verify against json schemas
      // TODO - pull in a json serlialization lib and parse into Order class
      TODO("Not yet implemented")
    }
  }
}

// TODO add the other message types - haven't done it while we figure out the structure/interface we want

fun main() {
  val rfqMessage = Rfq.create("pfi", "alice", 20)
  println("RfqMessage: $rfqMessage")

  val orderMessage = Order.create("pfi", "alice")
  println("OrderMessage: $orderMessage")

  // TODO add example usage of parse
}



