package tbdex.sdk.protocol.models

import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.Validator
import tbdex.sdk.protocol.models.Close.Companion.create
import tbdex.sdk.protocol.models.Order.Companion.create
import tbdex.sdk.protocol.validateExchangeId
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
  override val validNext: Set<MessageKind> = setOf(MessageKind.orderstatus, MessageKind.close)

  companion object {
    /**
     * Creates a new `Order` message, autopopulating the id, creation time, and message kind.
     *
     * @param to DID that the message is being sent to.
     * @param from DID of the sender.
     * @param exchangeId ID of the exchange.
     * @return Order instance.
     */
    fun create(to: String, from: String, exchangeId: String, externalId: String? = null): Order {
      validateExchangeId(exchangeId)

      val metadata = MessageMetadata(
        kind = MessageKind.order,
        to = to,
        from = from,
        id = TypeId.generate(MessageKind.order.name).toString(),
        exchangeId = exchangeId,
        createdAt = OffsetDateTime.now(),
        externalId = externalId
      )
      return Order(metadata, OrderData())
    }
  }
}