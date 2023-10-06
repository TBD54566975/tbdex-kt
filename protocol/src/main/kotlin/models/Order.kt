package models

import models.Close.Companion.create
import models.Order.Companion.create
import typeid.TypeID
import java.time.OffsetDateTime

/**
 * A class representing the Order message.
 * `Order` implements the [Message] class and contains close specific data
 * - Create message ([create])
 *
 * ### Example Usage:
 * ```kotlin
 * val order = Order.create(metadata, data)
 * ```
 */
class Order private constructor(
  override val metadata: MessageMetadata,
  override val data: OrderData,
  override var signature: String? = null
) : Message() {
  companion object {
    /**
     * Creates a new `Order` message, autopopulating the id, creation time, and message kind.
     *
     * @param to DID that the message is being sent to.
     * @param from DID of the sender.
     * @param exchangeId ID of the exchange.
     * @return Order instance.
     */
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
  }
}