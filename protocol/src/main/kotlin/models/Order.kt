package models

import Mapper
import typeid.TypeID
import java.time.OffsetDateTime

class OrderData : MessageData
class Order private constructor(
  metadata: MessageMetadata,
  data: OrderData,
  signature: String? = null
) : Message<OrderData>(metadata, data, signature) {
  companion object {
    fun create(to: String, from: String, exchangeId: TypeID): Order {
      val metadata = MessageMetadata(
        kind = MessageKind.order,
        to = to,
        from = from,
        id = TypeID(MessageKind.order.name),
        exchangeId = exchangeId,
        createdAt = OffsetDateTime.now()
      )
      return Order(metadata, OrderData())
    }

    fun parse(data: String): Order {
      // TODO verify the signature
      // TODO verify against json schemas

      return Mapper.reader(Order::class.java).readValue(data)
    }
  }
}