package tbdex.sdk.protocol.models

import tbdex.sdk.protocol.models.Close.Companion.create
import tbdex.sdk.protocol.models.OrderStatus.Companion.create
import typeid.TypeID
import java.time.OffsetDateTime

/**
 * A class representing the OrderStatus message.
 * `OrderStatus` implements the [Message] class and contains close specific data
 * - Create message ([create])
 *
 * ### Example Usage:
 * ```kotlin
 * val orderStatus = OrderStatus.create(metadata, data)
 * ```
 */
class OrderStatus private constructor(
  override val metadata: MessageMetadata,
  override val data: OrderStatusData,
  override var signature: String? = null
) : Message() {
  companion object {
    /**
     * Creates a new `OrderStatus` message, autopopulating the id, creation time, and message kind.
     *
     * @param to DID that the message is being sent to.
     * @param from DID of the sender.
     * @param exchangeId ID of the exchange.
     * @param orderStatusData Specific parameters relevant to an OrderStatus.
     * @return OrderStatus instance.
     */
    fun create(to: String, from: String, exchangeId: TypeID, orderStatusData: OrderStatusData): OrderStatus {
      val metadata = MessageMetadata(
        kind = MessageKind.orderstatus,
        to = to,
        from = from,
        id = TypeID(MessageKind.orderstatus.name),
        exchangeId = exchangeId,
        createdAt = OffsetDateTime.now()
      )
      return OrderStatus(metadata, orderStatusData)
    }
  }
}