package models

import Mapper
import typeid.TypeID
import java.time.OffsetDateTime

class Order private constructor(val data: OrderData, val metadata: MessageMetadata, var signature: String? = null) {
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
      return Order(OrderData(), metadata)
    }

    fun parse(data: String): Order {
      // TODO verify the signature
      // TODO verify against json schemas

      return Mapper.reader(Order::class.java).readValue(data)
    }
  }
}