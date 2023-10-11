package models

import typeid.TypeID
import java.time.OffsetDateTime

class Order private constructor(
  override val metadata: MessageMetadata,
  override val data: OrderData,
  override var signature: String? = null
) : Message() {
  companion object {
    fun create(to: String, from: String, exchangeId: TypeID): Order {
      val metadata = MessageMetadata(
        kind = MessageKind.order,
        to = to,
        from = from,
        id = TypeID(MessageKind.order.name).toString(),
        exchangeId = exchangeId.toString(),
        createdAt = OffsetDateTime.now()
      )
      return Order(metadata, OrderData())
    }
  }
}