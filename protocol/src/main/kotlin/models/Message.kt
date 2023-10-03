package models

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import typeid.TypeID
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

// Example to show we can use all lowercase (for typeID compat) via the name property
object OrderStatusKind: MessageKind {
  override val name: String = "orderstatus"
}

class MessageMetadata(
  val kind: MessageKind,
  val to: String,
  val from: String,
  val id: TypeID,
  val exchangeId: TypeID,
  val createdAt: OffsetDateTime
)

// TODO signature and sign seem ripe to be abstracted into a base class
class Rfq private constructor(val data: RfqData, val metadata: MessageMetadata, var signature: String? = null) {
  // TODO - use web5 crypto and fix the types
  fun sign(privateKey: String, kid: String) {
    this.signature = "blah"
  }

  companion object {
    // TODO is it best practice to share a mapper across different classes?
    private val mapper: ObjectMapper = ObjectMapper()
      .registerKotlinModule()

    fun create(to: String, from: String, amount: Int): Rfq {
      val id = TypeID(RfqKind.name)
      val metadata = MessageMetadata(
        kind = RfqKind,
        to = to,
        from = from,
        id = id,
        exchangeId = id,
        createdAt = OffsetDateTime.now(),
      )

      val data = RfqData(amount)
      return Rfq(data, metadata)
    }

    fun parse(data: String): Rfq {
      // TODO verify the signature
      // TODO verify against json schemas

      // TODO not validated, do we need to read the subtypes individually? (metadata and data)
      return mapper.readValue<Rfq>(data)
    }
  }
}

class Order private constructor(val data: OrderData, val metadata: MessageMetadata, var signature: String? = null) {
  // TODO - use web5 crypto and fix the types
  fun sign(privateKey: String, kid: String) {
    this.signature = "blah"
  }

  companion object {
    // TODO is it best practice to share a mapper across different classes?
    private val mapper: ObjectMapper = ObjectMapper()
      .registerKotlinModule()

    fun create(to: String, from: String, exchangeId: TypeID): Order {
      val metadata = MessageMetadata(
        kind = OrderKind,
        to = to,
        from = from,
        id = TypeID(OrderKind.name),
        exchangeId = exchangeId,
        createdAt = OffsetDateTime.now()
      )
      return Order(OrderData(), metadata)
    }

    fun parse(data: String): Order {
      // TODO verify the signature
      // TODO verify against json schemas

      return mapper.readValue<Order>(data)
    }
  }
}

// TODO add the other message types - haven't done it while we figure out the structure/interface we want

fun main() {
  val rfqMessage = Rfq.create("pfi", "alice", 20)
  println("RfqMessage: $rfqMessage")

  val orderMessage = Order.create("pfi", "alice", rfqMessage.metadata.exchangeId)
  println("OrderMessage: $orderMessage")

  // TODO add example usage of parse
}



