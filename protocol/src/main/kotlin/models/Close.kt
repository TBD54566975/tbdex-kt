package models

import models.Close.Companion.create
import typeid.TypeID
import java.time.OffsetDateTime

/**
 * A class representing the Close message.
 * `Close` implements the [Message] class and contains close specific data
 * - Create message ([create])
 *
 * ### Example Usage:
 *
 * ```kotlin
 * val close = Close.create(metadata, data)
 * ```
 */
class Close private constructor(
  override val metadata: MessageMetadata,
  override val data: CloseData,
  override var signature: String? = null
) : Message() {
  companion object {
    /**
     * Creates a new `Close` message, autopopulating the id, creation time, and message kind.
     *
     * @param to DID that the message is being sent to.
     * @param from DID of the sender.
     * @param exchangeId ID of the exchange.
     * @param closeData Specific parameters relevant to a Close.
     * @return Close instance.
     */
    fun create(to: String, from: String, exchangeId: TypeID, closeData: CloseData): Close {
      val metadata = MessageMetadata(
        kind = MessageKind.close,
        to = to,
        from = from,
        id = TypeID(MessageKind.close.name),
        exchangeId = exchangeId,
        createdAt = OffsetDateTime.now()
      )
      return Close(metadata, closeData)
    }
  }
}