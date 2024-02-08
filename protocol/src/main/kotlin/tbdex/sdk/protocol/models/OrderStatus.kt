package tbdex.sdk.protocol.models

import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.Validator
import tbdex.sdk.protocol.models.Close.Companion.create
import tbdex.sdk.protocol.models.OrderStatus.Companion.create
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
  override val validNext: Set<MessageKind> = setOf(MessageKind.orderstatus, MessageKind.close)

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
    fun create(
      to: String,
      from: String,
      exchangeId: TypeId,
      orderStatusData: OrderStatusData,
      externalId: String? = null
    ): OrderStatus {
      val metadata = MessageMetadata(
        kind = MessageKind.orderstatus,
        to = to,
        from = from,
        id = TypeId.generate(MessageKind.orderstatus.name),
        exchangeId = exchangeId,
        createdAt = OffsetDateTime.now(),
        externalId = externalId
      )
      Validator.validateData(orderStatusData, "orderstatus")

      return OrderStatus(metadata, orderStatusData)
    }
  }
}